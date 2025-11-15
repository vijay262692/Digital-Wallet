package com.digitalwallet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendActivationMail(String to, String token) {
        String activationLink = "http://localhost:8080/api/user/activate?token=" + token;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Activate Your Digital Wallet Account");
        msg.setText(
                "Welcome!\n\n" +
                "Your account has been created.\n" +
                "Click below to activate it:\n\n" +
                activationLink +
                "\n\nIf you did not request this, ignore this email."
        );

        mailSender.send(msg);
    }
}

