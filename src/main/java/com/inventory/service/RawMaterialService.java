package com.inventory.service;

import com.inventory.model.RawMaterial;
import com.inventory.model.RawMaterialCost;
import com.inventory.model.Production;
import com.inventory.repository.RawMaterialRepository;
import com.inventory.repository.RawMaterialCostRepository;
import com.inventory.dto.ProcessedRawMaterialReport;
import com.inventory.model.ProcessedRawMaterial;
import com.inventory.repository.ProcessedRawMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.inventory.model.Account;
import com.inventory.repository.AccountRepository;
import com.inventory.model.TransactionType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RawMaterialService {
    private final RawMaterialRepository rawMaterialRepository;
    private final ProcessedRawMaterialRepository processedRepository;
    private final AccountRepository accountRepository;
    
    @Transactional
    public RawMaterial createRawMaterial(RawMaterial rawMaterial) {
        // Generate unique 5-digit receipt number
        String receiptNumber;
        do {
            long count = rawMaterialRepository.count() + 1;
            receiptNumber = String.format("%05d", count);
        } while (rawMaterialRepository.existsByReceiptNumber(receiptNumber));
        
        rawMaterial.setReceiptNumber(receiptNumber);
        
        // Calculate CFT if not set
        if (rawMaterial.getCft() == null) {
            double cft = (rawMaterial.getLength() * rawMaterial.getBreadth() * rawMaterial.getHeight()) / 144.0;
            rawMaterial.setCft(cft);
        }
        
        return rawMaterialRepository.save(rawMaterial);
    }
    
    @Transactional
    public RawMaterial saveRawMaterial(RawMaterial rawMaterial) {
        return rawMaterialRepository.save(rawMaterial);
    }
    
    public List<RawMaterial> getRecentEntries() {
        return rawMaterialRepository.findTop10ByOrderByLorryInTimeDesc();
    }
    
    public List<RawMaterial> getUnprocessedEntries() {
        return rawMaterialRepository.findByAccountsProcessedFalse();
    }
    
    public java.util.Optional<RawMaterial> findById(Long id) {
        return rawMaterialRepository.findById(id);
    }
    
    @Transactional
    public ProcessedRawMaterial processRawMaterial(Long rawMaterialId, Double cost, String supervisor) {
        RawMaterial rawMaterial = rawMaterialRepository.findById(rawMaterialId)
            .orElseThrow(() -> new RuntimeException("Raw Material not found"));
            
        log.info("Processing raw material with ID: {}, Receipt #: {}", 
                rawMaterial.getId(), rawMaterial.getReceiptNumber());
            
        // Check if already processed
        if (Boolean.TRUE.equals(rawMaterial.getAccountsProcessed())) {
            throw new RuntimeException("Raw Material already processed");
        }
        
        LocalDateTime now = LocalDateTime.now();
        log.info("Processing date: {}", now);
        
        // Create ProcessedRawMaterial entry
        ProcessedRawMaterial processed = new ProcessedRawMaterial();
        processed.setRawMaterial(rawMaterial);
        processed.setCost(cost);
        processed.setAccountsSupervisor(supervisor);
        processed.setProcessedDate(now);
        
        // Save ProcessedRawMaterial first
        ProcessedRawMaterial savedProcessed = processedRepository.save(processed);
        log.info("Saved ProcessedRawMaterial with ID: {}", savedProcessed.getId());
        
        // Create Account entry
        Account account = new Account();
        account.setAmount(cost);
        account.setCost(cost);
        account.setDescription("Raw Material Processing - Receipt #" + rawMaterial.getReceiptNumber());
        account.setIsExpense(true);
        account.setProcessedAt(now);
        account.setRawMaterial(rawMaterial);
        account.setReferenceNumber(rawMaterial.getReceiptNumber());
        account.setSupervisorName(supervisor);
        account.setTransactionDate(now);
        account.setTransactionType(TransactionType.RAW_MATERIAL_PROCESSING);
        
        // Update raw material status
        rawMaterial.setAccountsProcessed(true);
        rawMaterialRepository.save(rawMaterial);
        
        // Save account entry
        Account savedAccount = accountRepository.save(account);
        log.info("Saved Account entry with ID: {}", savedAccount.getId());
        
        return savedProcessed;
    }
    
    public ProcessedRawMaterialReport getProcessedReport(LocalDate date) {
        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = date.plusDays(1).atStartOfDay();
        
        log.info("Fetching processed entries between {} and {}", startDate, endDate);
        
        List<ProcessedRawMaterial> processedEntries = processedRepository.findByProcessedDateBetween(startDate, endDate);
        
        log.info("Found {} processed entries", processedEntries.size());
        if (processedEntries.isEmpty()) {
            // Add a query to check if there are any entries at all
            long totalCount = processedRepository.count();
            log.info("Total entries in processed_raw_materials table: {}", totalCount);
        }
        
        return ProcessedRawMaterialReport.builder()
            .date(date)
            .processedEntries(processedEntries)
            .build();
    }
} 