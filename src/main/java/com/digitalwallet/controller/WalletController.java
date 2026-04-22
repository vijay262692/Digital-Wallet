package com.digitalwallet.controller;

import com.digitalwallet.common.CryptoUtil;
import com.digitalwallet.common.KeyManager;
import com.digitalwallet.broker.PNOBroker;
import com.digitalwallet.model.*;
import com.digitalwallet.repository.*;
import com.digitalwallet.service.EmailService;
import com.digitalwallet.service.WhatsAppService;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.PdfWriter;
import com.razorpay.RazorpayClient;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.security.KeyPair;
import java.util.*;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Element;

import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;

import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

import com.razorpay.Order;


@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired private KeyManager keyManager;
    @Autowired private PNOBroker pnoBroker;

    @Autowired private UserRepository userRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private CardRepository cardRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;
    @Autowired private WhatsAppService whatsAppService;

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
            
            // ⭐ BLOCK CARD ADD IF PIN IS NOT SET
            if (user.getWalletPin() == null) {
                response.put("status", "ERROR");
                response.put("message", "Please set your Wallet PIN before adding a card.");
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
            String pin = String.valueOf(request.get("pin"));   // ⭐ NEW FIELD
            String merchant = String.valueOf(request.get("merchant"));

            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                response.put("status", "ERROR");
                response.put("message", "User not found: " + username);
                return response;
            }
            
            // BLOCK PAYMENT IF PIN NOT SET
            if (user.getWalletPin() == null) {
                response.put("status", "ERROR");
                response.put("message", "Wallet PIN not set. Please set PIN before making payment.");
                return response;
            }
            
            // VALIDATE PIN (BCrypt)
            if (pin == null || pin.trim().isEmpty()) {
                response.put("status", "ERROR");
                response.put("message", "PIN is required for payment.");
                return response;
            }
            
            if (!passwordEncoder.matches(pin, user.getWalletPin())) {
                response.put("status", "ERROR");
                response.put("message", "Invalid PIN. Please try again.");
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

            record.setStatus("SUCCESS");


            record.setReferenceId(UUID.randomUUID().toString());
            record.setChannel("CARD");
            transactionRepository.save(record);

            
         //  send email + CSV statement
            try {
                // All transactions for this user (for statement)
           //     List<TransactionRecord> userTxns =
                        transactionRepository.findByUserUsernameOrderByTimestampDesc(username);
                
                List<TransactionRecord> userTxns = List.of(record); // ONLY CURRENT TXN

                String emailBody =
                        "Hello " + user.getUsername() + ",\n\n" +
                        "Your payment was successful.\n\n" +
                        "Amount   : ₹" + String.format("%.2f", amount) + "\n" +
                        "Merchant : " + merchant + "\n" +
                        "Provider : " + provider + "\n" +
                        "Card     : " + card.getMaskedPan() + "\n" +
                        "Txn ID   : " + record.getId() + "\n\n" +
                        "Your latest transaction statement is attached as a CSV file.\n\n" +
                        "Thanks,\nDigital Wallet Team";

                emailService.sendPaymentReceiptWithCsvAndPdf(
                        user.getEmail(),
                        user.getUsername(),
                        emailBody,
                        userTxns
                );
                
                try {
                    whatsAppService.sendPaymentMessage("919019157725", record);
                    // hardcoded the meta verified whatsapp cloud api number 
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } catch (Exception mailEx) {
            	
                // Don’t break payment if mail fails, just log
            	
                mailEx.printStackTrace();
            }
            
            String message = String.format("[%s] Payment of ₹%.2f processed at %s via token %s",
                    provider, amount, merchant, token);
            
            response.put("status", "SUCCESS");
            response.put("message", message);
            response.put("merchant", merchant);
            response.put("amount", amount);
            response.put("provider", provider);
            response.put("card", card.getMaskedPan());
            
            response.put("txnId", record.getId());
          //  response.put("date", record.getTimestamp().toString());
            
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            response.put("date", record.getTimestamp().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime()
                    .format(formatter));
            response.put("timestamp", record.getTimestamp().toString());
            return response;

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Transaction failed: " + e.getMessage());
            return response;
        }
    }

    
    @PostMapping("/create-order")
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> req) throws Exception {

        int amount = (int) Double.parseDouble(req.get("amount").toString()) * 100; // paise

        RazorpayClient client = new RazorpayClient("rzp_test_Se28Uscn7MSD6b", "P172dbolGN48woEHr4mJshY0");

        JSONObject options = new JSONObject();
        options.put("amount", amount);
        options.put("currency", "INR");
        options.put("receipt", "txn_" + System.currentTimeMillis());

        Order order = client.orders.create(options);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.get("id"));
        response.put("amount", amount);
        response.put("key", "rzp_test_Se28Uscn7MSD6b");

        return response;
    }
    
    @PostMapping("/verify-payment")
    public Map<String, Object> verifyPayment(@RequestBody Map<String, Object> req) {

        String username = (String) req.get("username");

        User user = userRepository.findByUsername(username).orElse(null);

        // Save transaction
        TransactionRecord record = new TransactionRecord();
        record.setAmount(Double.parseDouble(req.get("amount").toString()));
        record.setMerchant((String) req.get("merchant"));
        record.setStatus("SUCCESS");
        record.setProvider("RAZORPAY");
        record.setToken((String) req.get("razorpayPaymentId"));
        record.setUser(user);

        transactionRepository.save(record);

        // FETCH ALL TRANSACTIONS (for CSV/PDF)
        List<TransactionRecord> transactions =
            transactionRepository.findByUserUsernameOrderByTimestampDesc(username);

        // EMAIL BODY
        String emailBody =
            "Hello " + user.getUsername() + ",\n\n" +
            "Your payment was successful.\n\n" +
            "Amount   : ₹" + record.getAmount() + "\n" +
            "Merchant : " + record.getMerchant() + "\n" +
            "Provider : Razorpay\n" +
            "Txn ID   : " + record.getId() + "\n\n" +
            "Thanks,\nDigital Wallet Team";

        // Send Mail with PDf and csv files attched
        emailService.sendPaymentReceiptWithCsvAndPdf(
                user.getEmail(),
                user.getUsername(),
                emailBody,
                transactions
        );
        
     // Send WhatsApp message
        try {
            whatsAppService.sendPaymentMessage("919019157725", record);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return Map.of(
            "status", "SUCCESS",
            "txnId", record.getId(),
            "amount", record.getAmount(),
            "merchant", record.getMerchant(),
            "provider", "RAZORPAY",
            "timestamp", record.getTimestamp().toInstant().toString()
        );
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
    
    @GetMapping(value = "/transactions/{username}/export", produces = "text/csv")
    public void exportTransactionsCsv(@PathVariable String username,
                                      HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        String fileName = "statement-" + username + ".csv";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        var txns = transactionRepository.findByUserUsernameOrderByTimestampDesc(username);

        PrintWriter writer = response.getWriter();
        writer.println("Txn ID,Date Time,Amount,Status,Merchant,Masked PAN,Provider,Token");

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (var tx : txns) {
            String id        = String.valueOf(tx.getId());
            String dateTime  = tx.getTimestamp() != null ? sdf.format(tx.getTimestamp()) : "";
            String amount    = String.valueOf(tx.getAmount());
            String status    = safeCsv(tx.getStatus());
            String merchant  = safeCsv(tx.getMerchant());
            String maskedPan = safeCsv(tx.getMaskedPan());
            String provider  = safeCsv(tx.getProvider());
            String token     = safeCsv(tx.getToken());

            writer.println(String.join(",", id, dateTime, amount, status, merchant, maskedPan, provider, token));
        }

        writer.flush();
    }

    @GetMapping(value = "/transactions/{username}/export/pdf", produces = "application/pdf")
    public void exportTransactionsPdf(@PathVariable String username,@RequestParam(required = false) String date,
                                     HttpServletResponse response) throws IOException {

        response.setContentType("application/pdf");
        String fileName = "statement-" + username + ".pdf";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        var txns = transactionRepository.findByUserUsernameOrderByTimestampDesc(username);
        
        if (date != null && !date.isEmpty()) {
            txns = txns.stream()
                    .filter(tx -> {
                        if (tx.getTimestamp() == null) return false;

                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        String txDate = sdf.format(tx.getTimestamp());

                        return txDate.equals(date);
                    })
                    .collect(Collectors.toList());
        }

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, response.getOutputStream());

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Digital Wallet Statement", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);
            document.add(new Paragraph("User: " + username));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);

            table.addCell("Date");
            table.addCell("Merchant");
            table.addCell("Card");
            table.addCell("Provider");
            table.addCell("Status");
            table.addCell("Amount");

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (var tx : txns) {
                table.addCell(tx.getTimestamp() != null ? sdf.format(tx.getTimestamp()) : "");
                table.addCell(tx.getMerchant());
                table.addCell(tx.getMaskedPan());
                table.addCell(tx.getProvider());
                table.addCell(tx.getStatus());
                table.addCell(String.valueOf(tx.getAmount()));
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String safeCsv(String value) {
        if (value == null) return "\"\"";
        String v = value.replace("\"", "\"\"");
        return "\"" + v + "\"";
    }
}

    

