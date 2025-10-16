package com.digitalwallet.mdes;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MDESService {
    public String tokenize(String pan, String originalPayload) {
        System.out.println("[MDES] received payload: " + originalPayload);
        // Simulate token issuance
        return "MC-TOKEN-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
