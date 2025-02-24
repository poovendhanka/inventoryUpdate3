package com.inventory.repository;

import com.inventory.model.Account;
import com.inventory.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end);
    List<Account> findByTransactionTypeAndTransactionDateBetween(
        TransactionType type, LocalDateTime start, LocalDateTime end);
}