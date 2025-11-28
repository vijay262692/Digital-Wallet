package com.digitalwallet.repository;

import com.digitalwallet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    
    Optional<User> findByActivationToken(String token);
    
    Optional<User> findByUsernameIgnoreCase(String username);


}
