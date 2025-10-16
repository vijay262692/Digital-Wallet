package com.digitalwallet.repository;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TransactionRepository {

    // 🧾 Simple in-memory transaction list
    private final List<Map<String, Object>> transactions = new ArrayList<>();

    // 💾 Save new transaction
    public void save(Map<String, Object> txn) {
        transactions.add(txn);
    }

    // 🔍 Retrieve all transactions
    public List<Map<String, Object>> findAll() {
        return new ArrayList<>(transactions);
    }
}
