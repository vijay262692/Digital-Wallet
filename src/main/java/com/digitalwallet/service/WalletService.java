package com.digitalwallet.service;

import com.digitalwallet.model.User;
import com.digitalwallet.model.TransactionRecord;
import com.digitalwallet.repository.UserRepository;
import com.digitalwallet.repository.TransactionRepository;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    // 🔹 TOP-UP (only balance update)
    public String topUp(String username, Double amount) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Double currentBalance = user.getWalletBalance();

        if (currentBalance == null) {
            currentBalance = 0.0;
        }

        user.setWalletBalance(currentBalance + amount);
        userRepository.save(user);

        return "Balance updated: ₹" + user.getWalletBalance();
    }

    // 🔹 PAYMENT (deduct + save transaction)
    public TransactionRecord pay(String username, Double amount, String merchant) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ check balance
        if (user.getWalletBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }

        // ✅ deduct balance
        user.setWalletBalance(user.getWalletBalance() - amount);
        userRepository.save(user);

        // ✅ create transaction
        TransactionRecord txn = new TransactionRecord();
        txn.setUser(user); // IMPORTANT
        txn.setAmount(amount);
        txn.setMerchant(merchant);
        txn.setStatus("SUCCESS");

        // optional fields
        txn.setChannel("WALLET");
        txn.setReferenceId(UUID.randomUUID().toString());

        return transactionRepository.save(txn);
    }
}