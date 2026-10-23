package com.digitalwallet.dto;

import javax.validation.constraints.NotBlank;

/**
 * Request used to issue and provision a digital card.
 * The card credentials are supplied as an RSA-encrypted payload and are
 * decrypted only in the backend. Raw PAN/CVV are never persisted.
 */
public class DigitalIssuanceRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String encryptedCardPayload;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEncryptedCardPayload() {
        return encryptedCardPayload;
    }

    public void setEncryptedCardPayload(String encryptedCardPayload) {
        this.encryptedCardPayload = encryptedCardPayload;
    }
}
