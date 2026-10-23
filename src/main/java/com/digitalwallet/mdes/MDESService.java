package com.digitalwallet.mdes;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MDESService {

    public String tokenize(String pan, String originalPayload) {
        System.out.println("[MDES] received payload: " + originalPayload);
        // Simulate token issuance
        return "MC-TOKEN-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Demo lifecycle adapter for Mastercard token operations.
     * Replace the body with the real MDES API call when production credentials
     * and certificates are available.
     */
    public Map<String, String> updateTokenStatus(String token, String status) {
        System.out.println("[MDES] token=" + token + ", status=" + status);
        Map<String, String> response = new LinkedHashMap<>();
        response.put("status", "SUCCESS");
        response.put("provider", "MDES");
        response.put("token", token);
        response.put("tokenStatus", status);
        return response;
    }
}
