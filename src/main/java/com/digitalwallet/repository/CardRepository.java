package com.digitalwallet.repository;

import com.digitalwallet.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
    List<Card> findByUserUsername(String username);
    Optional<Card> findByToken(String token);
}
