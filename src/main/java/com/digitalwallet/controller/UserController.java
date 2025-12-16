package com.digitalwallet.controller;

import com.digitalwallet.common.CryptoUtil;
import com.digitalwallet.common.KeyManager;
import com.digitalwallet.model.User;
import com.digitalwallet.service.EmailService;
import com.digitalwallet.service.RefreshTokenService;
import com.digitalwallet.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.bind.annotation.*;
import com.digitalwallet.model.RefreshToken;
import org.springframework.transaction.annotation.Transactional;


import java.security.KeyPair;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;
import java.util.Random;



@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private KeyManager keyManager;

    @Autowired
    private UserRepository userRepository;
    
    
    @Autowired
    private EmailService emailService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepo;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private CardRepository cardRepository;
    
    @Autowired
    private TransactionRepository transactionRepository; 
    
    @Autowired
    private WalletRepository walletRepository;  

    /**
     *  Returns current RSA public key for encrypting login/register credentials
     */
    @GetMapping("/publicKey")
    public String getPublicKey() {
        return CryptoUtil.publicKeyToBase64(keyManager.getKeyPair().getPublic());
    }
    
    
    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    /**
     *  Register a new user
     */
	/*
	 * @PostMapping(value = "/register", consumes = MediaType.TEXT_PLAIN_VALUE)
	 * public Map<String, Object> register(@RequestBody String encryptedBase64) {
	 * Map<String, Object> response = new LinkedHashMap<>(); try { KeyPair kp =
	 * keyManager.getKeyPair(); String plain =
	 * CryptoUtil.decryptBase64RSA(encryptedBase64.trim(), kp.getPrivate());
	 * 
	 * // Expected format: username|email|password|role String[] parts =
	 * plain.split("\\|"); String username = parts[0]; String email = parts[1];
	 * String password = parts[2]; String role = parts.length > 3 ? parts[3] :
	 * "USER";
	 * 
	 * // Updated check using JPA Optional if
	 * (userRepository.findByUsername(username).isPresent()) {
	 * response.put("status", "ERROR"); response.put("message",
	 * "Username already exists!"); return response; }
	 * 
	 * User user = new User(username, email, password, role);
	 * userRepository.save(user);
	 * 
	 * response.put("status", "SUCCESS"); response.put("message",
	 * "User registered successfully!"); return response; } catch (Exception e) {
	 * response.put("status", "ERROR"); response.put("message",
	 * "Registration failed: " + e.getMessage()); return response; } }
	 */


    @PostMapping(value = "/register", consumes = MediaType.TEXT_PLAIN_VALUE)
    public Map<String, Object> register(@RequestBody String encryptedBase64) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            KeyPair kp = keyManager.getKeyPair();
            String plain = CryptoUtil.decryptBase64RSA(encryptedBase64.trim(), kp.getPrivate());

            String[] parts = plain.split("\\|");
            String username = parts[0].trim();
            String email = parts[1].trim();
            String password = parts[2];
            String role = parts.length > 3 ? parts[3] : "USER";

            if (userRepository.findByUsername(username).isPresent()) {
                response.put("status", "ERROR");
                response.put("message", "Username already exists!");
                return response;
            }

            // Generate activation token
            String token = UUID.randomUUID().toString();

            User user = new User(username, email, password, role);
            user.setActivated(false);
            user.setActivationToken(token);
            userRepository.save(user);

            // Send email
            emailService.sendActivationMail(email, token);

            response.put("status", "SUCCESS");
            response.put("message", "User registered. Check email to activate.");
            return response;

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Registration failed: " + e.getMessage());
            return response;
        }
    }
    

 @DeleteMapping("/terminate/{username}")
 @Transactional
 public Map<String, Object> terminateAccount(@PathVariable String username) {
     Map<String, Object> res = new LinkedHashMap<>();

     try {
         String normalized = username.trim();
         System.out.println("🛑 Terminate request for username = [" + normalized + "]");

         User user = userRepository.findByUsernameIgnoreCase(normalized).orElse(null);

         if (user == null) {
             res.put("status", "ERROR");
             res.put("message", "User not found");
             return res;
         }

         String email = user.getEmail();
         String name  = user.getUsername();

         //  Delete refresh tokens
         refreshTokenRepo.deleteByUser(user);

         // Delete cards of this user
         cardRepository.deleteByUser(user);

         
      // Delete transactions 
         transactionRepository.deleteByUser(user);
         
      // Delete wallet(s)
         walletRepository.deleteByUser(user);

         // delete user
         userRepository.delete(user);

         //Send email after successful delete
         emailService.sendEmail(
                 email,
                 "Your Digital Wallet Account Has Been Terminated",
                 "Hello " + name + ",\n\n" +
                 "Your Digital Wallet account has been successfully terminated.\n" +
                 "All tokens, saved cards and access permissions have been removed permanently.\n\n" +
                 "If you believe this was a mistake, please contact support.\n\n" +
                 "Regards,\nDigital Wallet Team"
         );

         res.put("status", "SUCCESS");
         res.put("message", "Account terminated & email notification sent.");
         return res;

     } catch (Exception e) {
         e.printStackTrace();
         res.put("status", "ERROR");
         res.put("message", "Account termination failed: " + e.getMessage());
         return res;
     }
 }


    
	/*
	 * @GetMapping("/activate") public String activateAccount(@RequestParam("token")
	 * String token) { Optional<User> optional =
	 * userRepository.findByActivationToken(token);
	 * 
	 * if (optional.isEmpty()) { return "Invalid or expired activation link!"; }
	 * 
	 * User user = optional.get(); user.setActivated(true);
	 * user.setActivationToken(null);
	 * 
	 * userRepository.save(user);
	 * 
	 * return "Account activated successfully! You can now login."; }
	 */
    
	/*
	 * @GetMapping("/activate") public RedirectView
	 * activateAccount(@RequestParam("token") String token) {
	 * 
	 * Optional<User> optional = userRepository.findByActivationToken(token);
	 * 
	 * // Invalid token if (!optional.isPresent()) { return new
	 * RedirectView("/activation-failed.html"); }
	 * 
	 * User user = optional.get(); user.setActivated(true);
	 * user.setActivationToken(null);
	 * 
	 * userRepository.save(user);
	 * 
	 * // Redirect to success page with username (displayed using JS) return new
	 * RedirectView("/activation-success.html?username=" + user.getUsername()); }
	 */
    
    
    @GetMapping("/activate")
    public RedirectView sendOtp(@RequestParam("token") String token) {

        Optional<User> optional = userRepository.findByActivationToken(token);

        if (optional.isEmpty()) {
            return new RedirectView("/activation-failed.html");
        }

        User user = optional.get();

        // Generate OTP
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5)); // OTP valid for 5 minutes

        userRepository.save(user);

        // Send OTP by email
        emailService.sendEmail(
                user.getEmail(),
                "Your Account Activation OTP",
                "Hello " + user.getUsername() + ",\n\nYour OTP is: " + otp +
                "\n\nValid for 5 minutes."
        );

        // After clicking email link → redirect to OTP page
        return new RedirectView("/otp-verify.html?username=" + user.getUsername());

    }

    
    @PostMapping("/verify-otp")
    public RedirectView verifyOtp(@RequestParam("username") String username,
                                  @RequestParam("otp") String otp) {

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return new RedirectView("/activation-failed.html");

        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            return new RedirectView("/otp-expired.html");
        }

        if (!otp.equals(user.getOtpCode())) {
            return new RedirectView("/otp-invalid.html");
        }

        // Success
        user.setActivated(true);
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        user.setActivationToken(null);

        userRepository.save(user);

        return new RedirectView("/set-pin.html?username=" + username);
    }
    
    
    @PostMapping("/set-pin")
    public Map<String, Object> setPin(@RequestBody Map<String, String> req) {
        Map<String, Object> res = new LinkedHashMap<>();

        try {
            String username = req.get("username");
            String pin = req.get("pin");

            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                res.put("status", "ERROR");
                res.put("message", "User not found");
                return res;
            }

            if (user.getWalletPin() != null) {
                res.put("status", "ERROR");
                res.put("message", "PIN already set — use change PIN");
                return res;
            }

            String hashedPin = passwordEncoder.encode(pin);
            user.setWalletPin(hashedPin);
            userRepository.save(user);
            
            // Send Activation mail to the user
            emailService.sendEmail(
                    user.getEmail(),
                    "Your Wallet Account is Activated",
                    "Hello " + user.getUsername() + ",\n\n" +
                    "Your account has been successfully activated.\n" +
                    "You can now use your Digital Wallet.\n\n" +
                    "Thanks,\nDigital Wallet Team"
            );

            res.put("status", "SUCCESS");
            res.put("message", "Wallet PIN set successfully");
            return res;

        } catch (Exception e) {
            res.put("status", "ERROR");
            res.put("message", e.getMessage());
            return res;
        }
    }

    
    @GetMapping("/has-pin/{username}")
    public Map<String, Object> hasPin(@PathVariable String username) {
        Map<String, Object> res = new HashMap<>();
        User user = userRepository.findByUsername(username).orElse(null);

        res.put("hasPin", user != null && user.getWalletPin() != null);
        return res;
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

			/*
			 * if (!user.isActive()) { response.put("status", "ERROR");
			 * response.put("message", "Account not active!"); return response; }
			 */
            
            if (!user.getActivated()) {
                response.put("status", "ERROR");
                response.put("message", "Account not activated! Check your email.");
                return response;
            }


            if (!user.getPassword().equals(password)) {
                response.put("status", "ERROR");
                response.put("message", "Invalid credentials!");
                return response;
            }

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
            response.put("refreshToken", refreshToken.getToken());
            
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
    
    @PostMapping("/refresh-token")
    public Map<String, Object> refreshAccessToken(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new LinkedHashMap<>();

        String token = body.get("refreshToken");

        RefreshToken rt = refreshTokenRepo.findByToken(token).orElse(null);

        if (rt == null) {
            response.put("status", "ERROR");
            response.put("message", "Invalid refresh token");
            return response;
        }

        if (refreshTokenService.isExpired(rt)) {
            response.put("status", "ERROR");
            response.put("message", "Refresh token expired");
            return response;
        }

        // Generate new access token 
        String newAccessToken = UUID.randomUUID().toString();

        response.put("status", "SUCCESS");
        response.put("accessToken", newAccessToken);
        return response;
    }

}
