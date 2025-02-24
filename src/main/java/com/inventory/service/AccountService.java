package com.inventory.service;

import com.inventory.model.Account;
import com.inventory.model.RawMaterial;
import com.inventory.model.Expense;
import com.inventory.model.TransactionType;
import com.inventory.repository.AccountRepository;
import com.inventory.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final ExpenseRepository expenseRepository;
    private final RawMaterialService rawMaterialService;
    
    @Transactional
    public Expense recordExpense(Expense expense) {
        return expenseRepository.save(expense);
    }
    
    @Transactional
    public Account processRawMaterial(Long rawMaterialId, Double cost, String supervisorName) {
        // Find and validate raw material
        RawMaterial rawMaterial = rawMaterialService.findById(rawMaterialId)
            .orElseThrow(() -> new RuntimeException("Raw material not found"));
            
        if (rawMaterial.getAccountsProcessed()) {
            throw new RuntimeException("Raw material already processed");
        }
        
        // Create and save account entry
        Account account = new Account();
        account.setRawMaterial(rawMaterial);
        account.setCost(cost);
        account.setSupervisorName(supervisorName);
        account.setProcessedAt(LocalDateTime.now());
        
        // Update raw material status
        rawMaterial.setAccountsProcessed(true);
        rawMaterialService.saveRawMaterial(rawMaterial);
        
        return accountRepository.save(account);
    }

    public List<Account> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end) {
        return accountRepository.findByTransactionDateBetween(start, end);
    }
    
    public List<Account> getTransactionsByTypeAndDateRange(
            TransactionType type, LocalDateTime start, LocalDateTime end) {
        return accountRepository.findByTransactionTypeAndTransactionDateBetween(type, start, end);
    }
    
    public Double calculateProfitLoss(LocalDateTime start, LocalDateTime end) {
        List<Account> transactions = getTransactionsByDateRange(start, end);
        return transactions.stream()
            .mapToDouble(t -> t.getIsExpense() ? -t.getAmount() : t.getAmount())
            .sum();
    }
}