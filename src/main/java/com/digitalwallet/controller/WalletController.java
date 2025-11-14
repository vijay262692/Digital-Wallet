package com.digitalwallet.controller;

import com.digitalwallet.common.CryptoUtil;
import com.digitalwallet.common.KeyManager;
import com.digitalwallet.broker.PNOBroker;
import com.digitalwallet.model.*;
import com.digitalwallet.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.util.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired private KeyManager keyManager;
    @Autowired private PNOBroker pnoBroker;

    @Autowired private UserRepository userRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private CardRepository cardRepository;
    @Autowired private TransactionRepository transactionRepository;

    @GetMapping("/publicKey")
    public String getPublicKey() {
        return CryptoUtil.publicKeyToBase64(keyManager.getKeyPair().getPublic());
    }

    // Ensure a wallet exists for the user (one per user)
    private Wallet ensureWallet(User user) {
        return walletRepository.findByUserUsername(user.getUsername())
                .orElseGet(() -> {
                    Wallet w = new Wallet();
                    w.setUser(user);
                    w.setBalance(BigDecimal.ZERO);
                    return walletRepository.save(w);
                });
    }

    @PostMapping(value = "/addCard/{username}", consumes = MediaType.TEXT_PLAIN_VALUE)
    public Map<String, Object> addCard(@PathVariable String username, @RequestBody String encryptedBase64) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                response.put("status", "ERROR");
                response.put("message", "User not found: " + username);
                return response;
            }

            KeyPair kp = keyManager.getKeyPair();
            String plain = CryptoUtil.decryptBase64RSA(encryptedBase64.trim(), kp.getPrivate());
            String[] parts = plain.split("\\|");
            String pan = parts.length > 0 ? parts[0] : "";
            String expiry = parts.length > 1 ? parts[1] : "";
            // cvv intentionally ignored (never store)

            Map<String, String> networkResponse = pnoBroker.routeAndTokenize(pan, plain);
            String token = networkResponse.get("token");
            String provider = networkResponse.getOrDefault("provider", "UNKNOWN");
            String maskedPan = "**** **** **** " + pan.substring(Math.max(0, pan.length() - 4));

            // store in DB
            Card card = new Card();
            card.setUser(user);
            card.setToken(token);
            card.setProvider(provider);
            card.setMaskedPan(maskedPan);
            card.setExpiry(expiry);
            card.setStatus("ACTIVE");
            cardRepository.save(card);

            response.put("status", "SUCCESS");
            response.put("token", token);
            response.put("provider", provider);
            response.put("maskedPan", maskedPan);
            response.put("expiry", expiry);
            response.put("message", "Card added successfully for user: " + username);
            return response;

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Failed to add card: " + e.getMessage());
            return response;
        }
    }

    @GetMapping(value = "/cards/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Card> getUserCards(@PathVariable String username) {
        return cardRepository.findByUserUsername(username);
    }
    
    
    
    @PostMapping(value = "/cardStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> updateCardStatus(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            String username = request.get("username");
            String token = request.get("token");
            String newStatus = request.get("status");

            if (username == null || token == null || newStatus == null) {
                response.put("status", "ERROR");
                response.put("message", "Missing required fields.");
                return response;
            }

            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                response.put("status", "ERROR");
                response.put("message", "User not found: " + username);
                return response;
            }

            Card card = cardRepository.findByToken(token).orElse(null);
            if (card == null) {
                response.put("status", "ERROR");
                response.put("message", "Card not found for token: " + token);
                return response;
            }

            // Only the owner of the card can update it
            if (!card.getUser().getUsername().equals(username)) {
                response.put("status", "ERROR");
                response.put("message", "Unauthorized: Card does not belong to user.");
                return response;
            }

            // Validate status
            if (!Arrays.asList("ACTIVE", "SUSPENDED", "TERMINATED").contains(newStatus.toUpperCase())) {
                response.put("status", "ERROR");
                response.put("message", "Invalid status. Allowed: ACTIVE, SUSPENDED, TERMINATED");
                return response;
            }

            // Update the status
            card.setStatus(newStatus.toUpperCase());
            cardRepository.save(card);

            response.put("status", "SUCCESS");
            response.put("message", "Card status updated to " + newStatus);
            response.put("token", token);

            return response;

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Failed: " + e.getMessage());
            return response;
        }
    }


    @PostMapping(value = "/pay", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> makePayment(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            String username = String.valueOf(request.get("username")); // add username to request
            String token = String.valueOf(request.get("token"));
            double amount = Double.parseDouble(String.valueOf(request.get("amount")));
            String merchant = String.valueOf(request.get("merchant"));

            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                response.put("status", "ERROR");
                response.put("message", "User not found: " + username);
                return response;
            }
            Wallet wallet = ensureWallet(user);

            Card card = cardRepository.findByToken(token).orElse(null);
            if (card == null) {
                response.put("status", "ERROR");
                response.put("message", "Card not found for token: " + token);
                return response;
            }
            
            
            if (!"ACTIVE".equalsIgnoreCase(card.getStatus())) {
                response.put("status", "ERROR");
                response.put("message", "Card is " + card.getStatus() + ". Payment cannot be processed.");
                return response;
            }

            String provider = card.getProvider();

            // (Optional) update wallet balance here (debit)
            // BigDecimal newBalance = wallet.getBalance().subtract(BigDecimal.valueOf(amount));
            // wallet.setBalance(newBalance);
            // walletRepository.save(wallet);

            String message = String.format("[%s] Payment of ₹%.2f processed at %s via token %s",
                    provider, amount, merchant, token);

            // save transaction in DB
            TransactionRecord record = new TransactionRecord();
            record.setToken(token);
            record.setAmount(amount);
            record.setMerchant(merchant);
            record.setProvider(provider);
            record.setMaskedPan(card.getMaskedPan());
            record.setStatus("SUCCESS");
            record.setUser(user);
            record.setWallet(wallet);
            transactionRepository.save(record);

            response.put("status", "SUCCESS");
            response.put("message", message);
            response.put("merchant", merchant);
            response.put("amount", amount);
            response.put("provider", provider);
            response.put("card", card.getMaskedPan());
            response.put("timestamp", record.getTimestamp().toString());
            return response;

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Transaction failed: " + e.getMessage());
            return response;
        }
    }

    @GetMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TransactionRecord> getTransactions() {
        List<TransactionRecord> list = transactionRepository.findAll();
        System.out.println("🔎 Fetching transactions from DB: " + list.size());
        return list;
    }

    @GetMapping(value = "/transactions/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TransactionRecord> getTransactionsByUser(@PathVariable String username) {
    	List<TransactionRecord> list = transactionRepository.findByUser_Username(username);
        System.out.println("🔎 Fetching transactions for user " + username + ": " + list.size());
        return list;

    }
    
    
}
