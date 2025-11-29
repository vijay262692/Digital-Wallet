package com.digitalwallet.repository;

import com.digitalwallet.model.TransactionRecord;
import com.digitalwallet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionRecord, UUID> {

    List<TransactionRecord> findByUser_Username(String username);
    
    List<TransactionRecord> findByUserUsernameOrderByTimestampDesc(String username);
    
    
    @Modifying
    @Transactional
    void deleteByUser(User user);
}
