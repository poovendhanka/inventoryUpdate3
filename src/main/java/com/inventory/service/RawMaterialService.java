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
import com.inventory.model.HuskType;
import com.inventory.service.StockService;

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
    private final StockService stockService;

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

    public List<RawMaterial> getRecentRawMaterials(int limit) {
        return rawMaterialRepository.findTopByOrderByLorryInTimeDesc(org.springframework.data.domain.PageRequest.of(0, limit)).getContent();
    }

    public List<RawMaterial> getRawMaterialsByDate(LocalDate date) {
        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = date.plusDays(1).atStartOfDay();
        return rawMaterialRepository.findByLorryInTimeBetweenOrderByLorryInTimeDesc(startDate, endDate);
    }

    @Transactional
    public void deleteRawMaterial(Long id) {
        RawMaterial rawMaterial = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Raw Material not found"));
        
        // Check if it's been processed
        if (Boolean.TRUE.equals(rawMaterial.getAccountsProcessed())) {
            throw new RuntimeException("Cannot delete processed raw material");
        }
        
        rawMaterialRepository.delete(rawMaterial);
    }

    public List<RawMaterial> getUnprocessedEntries() {
        return rawMaterialRepository.findByAccountsProcessedFalse();
    }

    public List<ProcessedRawMaterial> getRecentProcessedEntries() {
        return processedRepository.findTop10ByOrderByProcessedDateDesc();
    }

    public List<ProcessedRawMaterial> getProcessedEntriesByDate(LocalDate date) {
        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = date.plusDays(1).atStartOfDay();
        
        log.info("Fetching processed entries for date: {}", date);
        List<ProcessedRawMaterial> entries = processedRepository.findByProcessedDateBetween(startDate, endDate);
        log.info("Found {} processed entries for date {}", entries.size(), date);
        
        return entries;
    }

    public ProcessedRawMaterial getProcessedById(Long id) {
        return processedRepository.findById(id).orElse(null);
    }

    public java.util.Optional<RawMaterial> findById(Long id) {
        return rawMaterialRepository.findById(id);
    }

    @Transactional
    public ProcessedRawMaterial processRawMaterial(Long rawMaterialId, Double costPerCft, String supervisor) {
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

        // Add husk stock if husk type and cft are present
        if (rawMaterial.getHuskType() != null && rawMaterial.getCft() != null) {
            stockService.addHuskStock(rawMaterial.getHuskType(), rawMaterial.getCft());
        }

        // Create ProcessedRawMaterial entry
        ProcessedRawMaterial processed = new ProcessedRawMaterial();
        processed.setRawMaterial(rawMaterial);
        processed.setCostPerCft(costPerCft);
        processed.setAccountsSupervisor(supervisor);
        processed.setProcessedDate(now);

        // Save ProcessedRawMaterial first (this will calculate totalCost automatically via @PrePersist)
        ProcessedRawMaterial savedProcessed = processedRepository.save(processed);
        log.info("Saved ProcessedRawMaterial with ID: {}", savedProcessed.getId());

        // Create Account entry using the calculated total cost
        Account account = new Account();
        account.setAmount(savedProcessed.getTotalCost());
        account.setCost(savedProcessed.getTotalCost());
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

        List<ProcessedRawMaterial> processedEntries = processedRepository.findByProcessedDateBetween(startDate,
                endDate);

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

    public ProcessedRawMaterialReport getProcessedReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        log.info("Fetching processed entries between {} and {}", startDateTime, endDateTime);

        List<ProcessedRawMaterial> processedEntries = processedRepository.findByProcessedDateBetween(startDateTime,
                endDateTime);

        log.info("Found {} processed entries", processedEntries.size());
        if (processedEntries.isEmpty()) {
            // Add a query to check if there are any entries at all
            long totalCount = processedRepository.count();
            log.info("Total entries in processed_raw_materials table: {}", totalCount);
        }

        // Calculate totals
        double totalCost = processedEntries.stream()
                .mapToDouble(processed -> processed.getTotalCost() != null ? processed.getTotalCost() : 0.0)
                .sum();

        return ProcessedRawMaterialReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .processedEntries(processedEntries)
                .totalCost(totalCost)
                .build();
    }
}