package com.digitalwallet.controller;

import com.digitalwallet.common.CryptoUtil;
import com.digitalwallet.common.KeyManager;
import com.digitalwallet.model.User;
import com.digitalwallet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.security.KeyPair;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private KeyManager keyManager;

    @Autowired
    private UserRepository userRepository;

    /**
     * ✅ Returns current RSA public key for encrypting login/register credentials
     */
    @GetMapping("/publicKey")
    public String getPublicKey() {
        return CryptoUtil.publicKeyToBase64(keyManager.getKeyPair().getPublic());
    }

    /**
     * ✅ Register a new user
     */
    @PostMapping(value = "/register", consumes = MediaType.TEXT_PLAIN_VALUE)
    public Map<String, Object> register(@RequestBody String encryptedBase64) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            KeyPair kp = keyManager.getKeyPair();
            String plain = CryptoUtil.decryptBase64RSA(encryptedBase64.trim(), kp.getPrivate());

            // Expected format: username|email|password|role
            String[] parts = plain.split("\\|");
            String username = parts[0];
            String email = parts[1];
            String password = parts[2];
            String role = parts.length > 3 ? parts[3] : "USER";

            // ✅ Updated check using JPA Optional
            if (userRepository.findByUsername(username).isPresent()) {
                response.put("status", "ERROR");
                response.put("message", "Username already exists!");
                return response;
            }

            User user = new User(username, email, password, role);
            userRepository.save(user);

            response.put("status", "SUCCESS");
            response.put("message", "User registered successfully!");
            return response;
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Registration failed: " + e.getMessage());
            return response;
        }
    }

    /**
     * ✅ User login
     */
    @PostMapping(value = "/login", consumes = MediaType.TEXT_PLAIN_VALUE)
    public Map<String, Object> login(@RequestBody String encryptedBase64) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            KeyPair kp = keyManager.getKeyPair();
            String plain = CryptoUtil.decryptBase64RSA(encryptedBase64.trim(), kp.getPrivate());

            // Expected format: username|password
            String[] parts = plain.split("\\|");
            String username = parts[0];
            String password = parts[1];

            // ✅ Get user from DB using Optional
            User user = userRepository.findByUsername(username).orElse(null);

            if (user == null) {
                response.put("status", "ERROR");
                response.put("message", "User not found!");
                return response;
            }

            if (!user.isActive()) {
                response.put("status", "ERROR");
                response.put("message", "Account not active!");
                return response;
            }

            if (!user.getPassword().equals(password)) {
                response.put("status", "ERROR");
                response.put("message", "Invalid credentials!");
                return response;
            }

            response.put("status", "SUCCESS");
            response.put("message", "Login successful!");
            response.put("role", user.getRole());
            return response;

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Login failed: " + e.getMessage());
            return response;
        }
    }
}
