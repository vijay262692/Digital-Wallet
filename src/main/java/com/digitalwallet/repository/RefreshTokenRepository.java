package com.digitalwallet.repository;
import com.digitalwallet.model.RefreshToken;
import com.digitalwallet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;


public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
