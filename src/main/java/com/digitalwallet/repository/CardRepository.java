package com.digitalwallet.repository;

import com.digitalwallet.model.Card;
import com.digitalwallet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
    List<Card> findByUserUsername(String username);
    Optional<Card> findByToken(String token);
    
    @Modifying
    @Transactional
    void deleteByUser(User user);
}
