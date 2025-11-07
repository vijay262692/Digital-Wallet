package com.digitalwallet.repository;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class CardRepository {

    // 🗃️ Store cards per user: username → list of cards
    private final Map<String, List<Map<String, Object>>> userCards = new HashMap<>();

    // 💾 Save a card for a specific user
    public void saveForUser(String username, Map<String, Object> cardInfo) {
        userCards.computeIfAbsent(username, k -> new ArrayList<>()).add(cardInfo);
    }

    // 📦 Get all cards for a specific user
    public List<Map<String, Object>> findAllForUser(String username) {
        return userCards.getOrDefault(username, new ArrayList<>());
    }

    // 🧾 (Optional) Get all cards for admin view
    public Map<String, List<Map<String, Object>>> findAllUsers() {
        return userCards;
    }
    
    public Map<String, Object> findByToken(String token) {
        // Search across all users’ stored cards for a matching token
        for (List<Map<String, Object>> cards : userCards.values()) {
            for (Map<String, Object> card : cards) {
                if (token.equals(card.get("token"))) {
                    return card;
                }
            }
        }
        return null; // Not found
    }
    
    
    public boolean updateCardStatus(String username, String token, String newStatus) {
        List<Map<String, Object>> cards = userCards.get(username);
        if (cards == null) return false;

        for (Map<String, Object> card : cards) {
            if (token.equals(card.get("token"))) {
                card.put("status", newStatus);
                card.put("lastUpdated", new Date().toString());
                return true;
            }
        }
        return false;
    }


}
