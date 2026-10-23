package com.digitalwallet.controller;

import com.digitalwallet.dto.DigitalIssuanceRequest;
import com.digitalwallet.service.DigitalIssuanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * Digital Card Issuance and Token Lifecycle APIs.
 */
@RestController
@RequestMapping("/api/issuance")
public class DigitalIssuanceController {

    @Autowired
    private DigitalIssuanceService digitalIssuanceService;

    /**
     * Issues a digital card and provisions a network token using the existing
     * VTS/MDES routing broker.
     */
    @PostMapping("/cards")
    public ResponseEntity<Map<String, Object>> issueDigitalCard(
            @Valid @RequestBody DigitalIssuanceRequest request) {
        Map<String, Object> response = digitalIssuanceService.issueDigitalCard(request);
        return ResponseEntity.ok(response);
    }

    /** Activate a provisioned token. */
    @PostMapping("/tokens/{token}/activate")
    public ResponseEntity<Map<String, Object>> activate(@PathVariable String token) {
        return ResponseEntity.ok(digitalIssuanceService.activate(token));
    }

    /** Suspend an active token. */
    @PostMapping("/tokens/{token}/suspend")
    public ResponseEntity<Map<String, Object>> suspend(@PathVariable String token) {
        return ResponseEntity.ok(digitalIssuanceService.suspend(token));
    }

    /** Resume a suspended token. */
    @PostMapping("/tokens/{token}/resume")
    public ResponseEntity<Map<String, Object>> resume(@PathVariable String token) {
        return ResponseEntity.ok(digitalIssuanceService.resume(token));
    }

    /** Permanently terminate a token. */
    @PostMapping("/tokens/{token}/terminate")
    public ResponseEntity<Map<String, Object>> terminate(@PathVariable String token) {
        return ResponseEntity.ok(digitalIssuanceService.terminate(token));
    }

    /** Retrieve token/card provisioning details. */
    @GetMapping("/tokens/{token}")
    public ResponseEntity<Map<String, Object>> getToken(@PathVariable String token) {
        return ResponseEntity.ok(digitalIssuanceService.getToken(token));
    }
}
