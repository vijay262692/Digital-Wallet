package com.digitalwallet.repository;

import com.digitalwallet.model.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionRecord, UUID> {

    List<TransactionRecord> findByUser_Username(String username);
}
