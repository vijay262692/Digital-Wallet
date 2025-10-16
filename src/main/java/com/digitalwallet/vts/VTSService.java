package com.digitalwallet.vts;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class VTSService {
    public String tokenize(String pan, String originalPayload) {
        System.out.println("[VTS] received payload: " + originalPayload);
        return "VISA-TOKEN-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
