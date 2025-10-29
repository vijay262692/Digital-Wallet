package com.digitalwallet.controller;

import com.digitalwallet.common.CryptoUtil;
import com.digitalwallet.common.KeyManager;
import com.digitalwallet.repository.CardRepository;
import com.digitalwallet.repository.TransactionRepository;
import com.digitalwallet.broker.PNOBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import com.digitalwallet.service.UserService;
import com.digitalwallet.model.User;

import java.security.KeyPair;
import java.util.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private KeyManager keyManager;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private PNOBroker pnoBroker;
    
    @Autowired 
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserService userService; 
    


    /**
     * Returns the current RSA public key (Base64 encoded)
     */
    @GetMapping("/publicKey")
    public String getPublicKey() {
        return CryptoUtil.publicKeyToBase64(keyManager.getKeyPair().getPublic());
    }

    /**
     * Accepts encrypted card data (Base64 RSA), decrypts it, routes via broker,
     * and stores masked card + token info.
     */
    @PostMapping(value = "/addCard/{username}", consumes = MediaType.TEXT_PLAIN_VALUE)
    public Map<String, Object> addCard(@PathVariable String username, @RequestBody String encryptedBase64) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            KeyPair kp = keyManager.getKeyPair();
            String plain = CryptoUtil.decryptBase64RSA(encryptedBase64.trim(), kp.getPrivate());
            String[] parts = plain.split("\\|");
            String pan = parts.length > 0 ? parts[0] : "";
            String expiry = parts.length > 1 ? parts[1] : "";
            String cvv = parts.length > 2 ? parts[2] : "";

            Map<String, String> networkResponse = pnoBroker.routeAndTokenize(pan, plain);

            response.putAll(networkResponse);
            response.put("maskedPan", "**** **** **** " + pan.substring(pan.length() - 4));
            response.put("expiry", expiry);
            response.put("cvv", "***");
            response.put("timestamp", new Date().toString());
            response.put("username", username);

            // Save card for that user
            cardRepository.saveForUser(username, response);

            response.put("status", "SUCCESS");
            response.put("message", "Card added successfully for user: " + username);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "ERROR");
            response.put("message", "Failed to add card: " + e.getMessage());
            return response;
        }
    }



    /**
     * Returns all stored cards (masked only)
     */
    @GetMapping(value = "/cards/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> getUserCards(@PathVariable String username) {
        return cardRepository.findAllForUser(username);
    }

    
    
    /**
     * Initiates a transaction using a saved tokenized card.
     * Expects JSON like:
     * {
     *   "token": "VISA-TOKEN-abc123",
     *   "amount": "249.50",
     *   "merchant": "Amazon"
     * }
     */
    @PostMapping(value = "/pay", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> makePayment(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            String token = request.get("token").toString();
            double amount = Double.parseDouble(request.get("amount").toString());
            String merchant = request.get("merchant").toString();

            Map<String, Object> card = cardRepository.findByToken(token);
            if (card == null) {
                response.put("status", "ERROR");
                response.put("message", "Card not found for token: " + token);
                return response;
            }

            String provider = card.get("provider").toString();
            String message = String.format("[%s] Payment of ₹%.2f processed at %s via token %s",
                    provider, amount, merchant, token);

            response.put("status", "SUCCESS");
            response.put("message", message);
            response.put("merchant", merchant);
            response.put("amount", amount);
            response.put("provider", provider);
            response.put("card", card.get("maskedPan"));
            response.put("timestamp", new Date().toString());

            // 💾 Add your debug lines here
            System.out.println("💾 Saving transaction to repo...");
            transactionRepository.save(response);
            System.out.println("📦 Total transactions now: " + transactionRepository.findAll().size());

            return response;

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Transaction failed: " + e.getMessage());
            return response;
        }
    }
    
    /**
    
    @PostMapping(value = "/addCard/{username}", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> addCard(@PathVariable String username, @RequestBody String encryptedBase64) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            KeyPair kp = keyManager.getKeyPair();
            String plain = CryptoUtil.decryptBase64RSA(encryptedBase64.trim(), kp.getPrivate());
            String[] parts = plain.split("\\|");
            String pan = parts[0];
            String expiry = parts[1];
            String cvv = parts[2];

            Map<String, String> networkResponse = pnoBroker.routeAndTokenize(pan, plain);

            response.putAll(networkResponse);
            response.put("maskedPan", "**** **** **** " + pan.substring(pan.length() - 4));
            response.put("expiry", expiry);
            response.put("cvv", "***");
            response.put("timestamp", new Date().toString());

            // Store under this user
            User user = userService.authenticate(username, null);
            if (user != null) user.addCard(response);

            cardRepository.save(response);
            return response;

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Decryption failed: " + e.getMessage());
            return response;
        }
    }
**/
    
    @GetMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> getTransactions() {
        System.out.println("🔎 Fetching transactions: " + transactionRepository.findAll().size());
        return transactionRepository.findAll();
    }


    
}
