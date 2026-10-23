package com.digitalwallet.service;

import com.digitalwallet.broker.PNOBroker;
import com.digitalwallet.common.CryptoUtil;
import com.digitalwallet.common.KeyManager;
import com.digitalwallet.dto.DigitalIssuanceRequest;
import com.digitalwallet.model.Card;
import com.digitalwallet.model.User;
import com.digitalwallet.repository.CardRepository;
import com.digitalwallet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DigitalIssuanceService {

    private static final String PENDING = "PENDING";
    private static final String ACTIVE = "ACTIVE";
    private static final String SUSPENDED = "SUSPENDED";
    private static final String TERMINATED = "TERMINATED";

    @Autowired
    private KeyManager keyManager;

    @Autowired
    private PNOBroker pnoBroker;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    /**
     * Issues a digital card and provisions a network token through the existing
     * PNO broker (VTS for Visa / MDES for Mastercard).
     */
    @Transactional
    public Map<String, Object> issueDigitalCard(DigitalIssuanceRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null) {
            return error("User not found: " + request.getUsername());
        }
        if (!user.isActive()) {
            return error("User account is inactive");
        }

        try {
            KeyPair keyPair = keyManager.getKeyPair();
            String encryptedPayload = request.getEncryptedCardPayload();
            String plainPayload = CryptoUtil.decryptBase64RSA(encryptedPayload.trim(), keyPair.getPrivate());

            String[] parts = plainPayload.split("\\|");
            String pan = parts.length > 0 ? parts[0].trim() : "";
            String expiry = parts.length > 1 ? parts[1].trim() : "";

            validateCardData(pan, expiry);

            Map<String, String> networkResponse = pnoBroker.routeAndTokenize(pan, plainPayload);
            if (!"SUCCESS".equalsIgnoreCase(networkResponse.get("status"))) {
                return error(networkResponse.getOrDefault("message", "Token provisioning failed"));
            }

            String token = networkResponse.get("token");
            String provider = networkResponse.getOrDefault("provider", "UNKNOWN");

            if (token == null || token.trim().isEmpty()) {
                return error("Token provider returned an empty token");
            }

            if (cardRepository.findByToken(token).isPresent()) {
                return error("Token already exists");
            }

            Card card = new Card();
            card.setUser(user);
            card.setToken(token);
            card.setProvider(provider);
            card.setMaskedPan(maskPan(pan));
            card.setExpiry(expiry);
            card.setStatus(PENDING);
            card.setBalance(10000.0);
            card = cardRepository.save(card);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Digital card issued and token provisioned successfully");
            response.put("cardId", card.getId());
            response.put("username", user.getUsername());
            response.put("maskedPan", card.getMaskedPan());
            response.put("provider", provider);
            response.put("token", token);
            response.put("tokenStatus", PENDING);
            return response;
        } catch (Exception e) {
            return error("Digital card issuance failed: " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> activate(String token) {
        return changeStatus(token, ACTIVE, "ACTIVATE");
    }

    @Transactional
    public Map<String, Object> suspend(String token) {
        return changeStatus(token, SUSPENDED, "SUSPEND");
    }

    @Transactional
    public Map<String, Object> resume(String token) {
        return changeStatus(token, ACTIVE, "RESUME");
    }

    @Transactional
    public Map<String, Object> terminate(String token) {
        return changeStatus(token, TERMINATED, "TERMINATE");
    }

    public Map<String, Object> getToken(String token) {
        Card card = cardRepository.findByToken(token).orElse(null);
        if (card == null) {
            return error("Token not found: " + token);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "SUCCESS");
        response.put("cardId", card.getId());
        response.put("token", card.getToken());
        response.put("provider", card.getProvider());
        response.put("maskedPan", card.getMaskedPan());
        response.put("expiry", card.getExpiry());
        response.put("tokenStatus", card.getStatus());
        response.put("username", card.getUser().getUsername());
        return response;
    }

    private Map<String, Object> changeStatus(String token, String targetStatus, String action) {
        Card card = cardRepository.findByToken(token).orElse(null);
        if (card == null) {
            return error("Token not found: " + token);
        }

        String currentStatus = card.getStatus() == null ? PENDING : card.getStatus().toUpperCase();
        if (!isValidTransition(currentStatus, targetStatus, action)) {
            return error("Invalid token lifecycle transition: " + currentStatus + " -> " + targetStatus);
        }

        Map<String, String> providerResponse = pnoBroker.updateTokenStatus(
                card.getProvider(), token, targetStatus
        );
        if (!"SUCCESS".equalsIgnoreCase(providerResponse.get("status"))) {
            return error(providerResponse.getOrDefault("message", "Token lifecycle update failed"));
        }

        card.setStatus(targetStatus);
        cardRepository.save(card);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Token " + action.toLowerCase() + " completed successfully");
        response.put("token", token);
        response.put("provider", card.getProvider());
        response.put("previousStatus", currentStatus);
        response.put("tokenStatus", targetStatus);
        return response;
    }

    private boolean isValidTransition(String current, String target, String action) {
        if (TERMINATED.equals(current)) {
            return false;
        }
        if ("ACTIVATE".equals(action)) {
            return PENDING.equals(current) && ACTIVE.equals(target);
        }
        if ("SUSPEND".equals(action)) {
            return ACTIVE.equals(current) && SUSPENDED.equals(target);
        }
        if ("RESUME".equals(action)) {
            return SUSPENDED.equals(current) && ACTIVE.equals(target);
        }
        if ("TERMINATE".equals(action)) {
            return (ACTIVE.equals(current) || SUSPENDED.equals(current)) && TERMINATED.equals(target);
        }
        return false;
    }

    private void validateCardData(String pan, String expiry) {
        if (pan == null || !pan.matches("\\d{13,19}")) {
            throw new IllegalArgumentException("Invalid PAN");
        }
        if (expiry == null || expiry.trim().isEmpty()) {
            throw new IllegalArgumentException("Card expiry is required");
        }
    }

    private String maskPan(String pan) {
        return "**** **** **** " + pan.substring(Math.max(0, pan.length() - 4));
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ERROR");
        response.put("message", message);
        return response;
    }
}
