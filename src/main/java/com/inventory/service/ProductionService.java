package com.inventory.service;

import com.inventory.model.Production;
import com.inventory.model.ShiftType;
import com.inventory.model.PithStock;
import com.inventory.model.FiberStock;
import com.inventory.repository.ProductionRepository;
import com.inventory.repository.PithStockRepository;
import com.inventory.repository.FiberStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductionService {
    
    private final ProductionRepository productionRepository;
    private final PithStockService pithStockService;
    private final FibreStockService fibreStockService;
    
    @Transactional
    public Production createProduction(Production production) {
        try {
            // Generate batch number
            Optional<Production> lastProduction = productionRepository.findFirstByProductionDateOrderByBatchNumberDesc(
                getAdjustedDate(production.getBatchCompletionTime())
            );
            int batchNumber = lastProduction.map(p -> p.getBatchNumber() + 1).orElse(1);
            production.setBatchNumber(batchNumber);
            
            // Save production
            Production savedProduction = productionRepository.save(production);
            
            // Update stocks - pith in kg, fiber in number of bales
            pithStockService.addStock(production.getPithQuantity());
            fibreStockService.addStock(Double.valueOf(production.getFiberBales()));
            
            return savedProduction;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create production: " + e.getMessage(), e);
        }
    }
    
    private LocalDate getAdjustedDate(LocalDateTime dateTime) {
        if (dateTime.getHour() >= 0 && dateTime.getHour() < 8) {
            return dateTime.toLocalDate().minusDays(1);
        }
        return dateTime.toLocalDate();
    }
    
    public List<Production> getProductionsByDateAndShift(LocalDate date, ShiftType shift) {
        List<Production> productions = productionRepository
            .findByProductionDateAndShiftOrderByBatchCompletionTimeAsc(date, shift);
        
        LocalDateTime previousBatchTime = null;
        for (Production prod : productions) {
            prod.calculateTimeTaken(previousBatchTime);
            previousBatchTime = prod.getBatchCompletionTime();
        }
        
        return productions;
    }
    
    public List<Production> getRecentProductions(LocalDate date) {
        return productionRepository.findByProductionDateOrderByBatchCompletionTimeDesc(date);
    }
} 