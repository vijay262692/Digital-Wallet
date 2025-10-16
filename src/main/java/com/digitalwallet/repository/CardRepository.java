package com.digitalwallet.repository;

import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class CardRepository {

    // 💾 Simple in-memory storage
    private final List<Map<String, Object>> storedCards = new ArrayList<>();

    // 🟢 Save a card entry (called by WalletController after tokenization)
    public void save(Map<String, Object> cardInfo) {
        storedCards.add(cardInfo);
    }

    // 🔵 Retrieve all stored cards
    public List<Map<String, Object>> findAll() {
        return new ArrayList<>(storedCards);
    }

    // 🔍 Find a card by its token
    public Map<String, Object> findByToken(String token) {
        if (token == null || token.isEmpty()) return null;
        for (Map<String, Object> card : storedCards) {
            Object storedToken = card.get("token");
            if (storedToken != null && storedToken.toString().equals(token)) {
                return card;
            }
        }
        return null; // not found
    }
}
