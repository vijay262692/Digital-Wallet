package com.digitalwallet.controller;

import com.digitalwallet.common.CryptoUtil;
import com.digitalwallet.common.KeyManager;
import com.digitalwallet.model.User;
import com.digitalwallet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.security.KeyPair;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private KeyManager keyManager;

    @Autowired
    private UserService userService;

    @GetMapping("/publicKey")
    public String getPublicKey() {
        return CryptoUtil.publicKeyToBase64(keyManager.getKeyPair().getPublic());
    }

    // 🟢 REGISTER
    @PostMapping(value = "/register", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> register(@RequestBody String encryptedBase64) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            KeyPair kp = keyManager.getKeyPair();
            String plain = CryptoUtil.decryptBase64RSA(encryptedBase64.trim(), kp.getPrivate());
            String[] parts = plain.split("\\|");
            String username = parts[0];
            String password = parts[1];

            if (userService.findByUsername(username) != null) {
                response.put("status", "ERROR");
                response.put("message", "User already exists");
                return response;
            }

            User user = new User(username, password, "USER");
            userService.registerUser(user);
            response.put("status", "SUCCESS");
            response.put("message", "User registered successfully");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }

    // 🔐 LOGIN
    @PostMapping(value = "/login", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> login(@RequestBody String encryptedBase64) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            KeyPair kp = keyManager.getKeyPair();
            String plain = CryptoUtil.decryptBase64RSA(encryptedBase64.trim(), kp.getPrivate());
            String[] parts = plain.split("\\|");
            String username = parts[0];
            String password = parts[1];

            User user = userService.findByUsername(username);
            if (user == null) {
                response.put("status", "ERROR");
                response.put("message", "Invalid credentials: user not found");
                return response;
            }

            // 🔍 compare passwords exactly
            if (!user.getPassword().equals(password)) {
                response.put("status", "ERROR");
                response.put("message", "Invalid credentials: wrong password");
                return response;
            }

            response.put("status", "SUCCESS");
            response.put("message", "Login successful");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
        }
        return response;
    }
}
