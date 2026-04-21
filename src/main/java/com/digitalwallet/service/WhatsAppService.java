package com.digitalwallet.service;

import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.digitalwallet.model.TransactionRecord;
import java.io.OutputStream;

@Service
public class WhatsAppService {

	@Value("${whatsapp.token}")
    private String token;

    @Value("${whatsapp.phoneNumberId}")
    private String phoneNumberId;

    public void sendPaymentMessage(String to, TransactionRecord record) {
        try {
            URL url = new URL("https://graph.facebook.com/v18.0/" + phoneNumberId + "/messages");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String message = String.format(
            	    "✅ Payment Successful\n\n" +
            	    "Merchant: %s\n" +
            	    "Amount: ₹%.2f\n" +
            	    "Status: SUCCESS\n" +
            	    "Card: %s\n" +
            	    "Provider: %s\n" +
            	    "Txn ID: %s\n" +
            	    "Date: %s",
            	    record.getMerchant(),
            	    record.getAmount(),
            	    record.getMaskedPan(),
            	    record.getProvider(),
            	    record.getId(),
            	    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            	        .format(record.getTimestamp())
            	);

            String body = String.format("""
            		{
            		  "messaging_product": "whatsapp",
            		  "to": "%s",
            		  "type": "text",
            		  "text": {
            		    "body": "%s"
            		  }
            		}
            		""", to, message.replace("\n", "\\n"));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes());
            }

            int responseCode = conn.getResponseCode();
            System.out.println("WhatsApp Response Code: " + responseCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}