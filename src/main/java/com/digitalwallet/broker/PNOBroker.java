package com.digitalwallet.broker;

import com.digitalwallet.mdes.MDESService;
import com.digitalwallet.vts.VTSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PNOBroker {

    @Autowired
    private MDESService mdesService;

    @Autowired
    private VTSService vtsService;

    public Map<String, String> routeAndTokenize(String pan, String originalPayload) {
        Map<String, String> resp = new HashMap<>();
        if (pan == null || pan.length() == 0) {
            resp.put("status", "ERROR");
            resp.put("message", "Invalid PAN");
            return resp;
        }
        char first = pan.charAt(0);
        if (first == '5') {
            String token = mdesService.tokenize(pan, originalPayload);
            resp.put("provider", "MDES");
            resp.put("token", token);
            resp.put("status", "SUCCESS");
        } else if (first == '4') {
            String token = vtsService.tokenize(pan, originalPayload);
            resp.put("provider", "VTS");
            resp.put("token", token);
            resp.put("status", "SUCCESS");
        } else {
            resp.put("status", "UNSUPPORTED");
            resp.put("message", "Only Visa (4...) and Mastercard (5...) supported in demo");
        }
        return resp;
    }

    /**
     * Routes token lifecycle operations to the same network provider that
     * originally provisioned the token.
     */
    public Map<String, String> updateTokenStatus(String provider, String token, String status) {
        if (provider == null || token == null || status == null) {
            return response("ERROR", "Provider, token and status are required");
        }

        if ("MDES".equalsIgnoreCase(provider)) {
            return mdesService.updateTokenStatus(token, status);
        }
        if ("VTS".equalsIgnoreCase(provider)) {
            return vtsService.updateTokenStatus(token, status);
        }
        return response("UNSUPPORTED", "Unsupported token provider: " + provider);
    }

    public String processPayment(String provider, String token, double amount, String merchant) {
        if ("MDES".equalsIgnoreCase(provider)) {
            return "[MDES] Payment of ₹" + amount + " processed at " + merchant + " via token " + token;
        } else if ("VTS".equalsIgnoreCase(provider)) {
            return "[VTS] Payment of ₹" + amount + " processed at " + merchant + " via token " + token;
        } else {
            return "Unknown provider";
        }
    }

    private Map<String, String> response(String status, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("status", status);
        response.put("message", message);
        return response;
    }
}
