package com.digitalwallet.repository;

import com.digitalwallet.model.Wallet;
import com.digitalwallet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByUserUsername(String username);
    
    @Modifying
    @Transactional
    void deleteByUser(User user);
}
