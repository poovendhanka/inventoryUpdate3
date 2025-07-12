package com.inventory.service;

import com.inventory.model.Production;
import com.inventory.model.ShiftType;
import com.inventory.model.PithStock;
import com.inventory.model.FiberStock;
import com.inventory.model.BlockProduction;
import com.inventory.model.CocopithProduction;
import com.inventory.model.PithType;
import com.inventory.model.HuskType;
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
import java.time.format.DateTimeFormatter;
import com.inventory.service.StockService;
import com.inventory.service.LooseFiberStockService;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductionService {

    private final ProductionRepository productionRepository;
    private final PithStockService pithStockService;
    private final LooseFiberStockService looseFiberStockService;
    private final StockService stockService;

    public void createProduction(Production production) {
        // Generate batch number
        Integer batchNumber = generateBatchNumber();
        production.setBatchNumber(batchNumber);

        // Save production (this will calculate loose fiber and CFT consumed via @PrePersist)
        Production savedProduction = productionRepository.save(production);

        // Update stocks based on production
        updateStocks(savedProduction);
    }

    private void updateStocks(Production production) {
        // Update pith stock
        if (production.getPithQuantity() != null && production.getPithQuantity() > 0) {
            pithStockService.addStock(production.getPithQuantity());
        }

        // Update loose fiber stock (new logic)
        if (production.getLooseFiberQuantity() != null && production.getLooseFiberQuantity() > 0) {
            looseFiberStockService.addStock(production.getLooseFiberQuantity(), production.getFiberType());
        }

        // Reduce husk stock based on CFT consumed (new logic)
        if (production.getCftConsumed() != null && production.getHuskType() != null) {
            stockService.reduceHuskStock(production.getHuskType(), production.getCftConsumed());
        }
    }

    private Integer generateBatchNumber() {
        return (int) (productionRepository.count() + 1);
    }

    public List<Production> getRecentProductions() {
        return productionRepository.findTop10ByOrderByBatchCompletionTimeDesc();
    }

    public List<Production> getRecentProduction(int limit) {
        return productionRepository.findTopByOrderByBatchCompletionTimeDesc(
            org.springframework.data.domain.PageRequest.of(0, limit)).getContent();
    }

    public List<Production> getProductionByDate(LocalDate date) {
        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = date.plusDays(1).atStartOfDay();
        return productionRepository.findByBatchCompletionTimeBetween(startDate, endDate);
    }

    @Transactional
    public void deleteProduction(Long id) {
        Production production = productionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Production not found"));
        
        // Reverse stock changes
        reverseStockChanges(production);
        
        productionRepository.delete(production);
    }

    private void reverseStockChanges(Production production) {
        // Validate stock availability before reversal
        validateStockForDeletion(production);
        
        // Reverse pith stock addition (reduce it)
        if (production.getPithQuantity() != null && production.getPithQuantity() > 0) {
            pithStockService.addStock(-production.getPithQuantity());
        }

        // Reverse loose fiber stock addition (reduce it)
        if (production.getLooseFiberQuantity() != null && production.getLooseFiberQuantity() > 0) {
            looseFiberStockService.addStock(-production.getLooseFiberQuantity(), production.getFiberType());
        }

        // Reverse husk stock reduction (add it back)
        if (production.getCftConsumed() != null && production.getHuskType() != null) {
            stockService.addHuskStock(production.getHuskType(), production.getCftConsumed());
        }
    }
    
    private void validateStockForDeletion(Production production) {
        // Check if we have enough pith stock to reduce
        if (production.getPithQuantity() != null && production.getPithQuantity() > 0) {
            Double currentPithStock = pithStockService.getCurrentStock();
            if (currentPithStock < production.getPithQuantity()) {
                throw new RuntimeException("Cannot delete production: Insufficient pith stock to reverse. " +
                    "Current: " + currentPithStock + " kg, Required: " + production.getPithQuantity() + " kg");
            }
        }
        
        // Check if we have enough loose fiber stock to reduce
        if (production.getLooseFiberQuantity() != null && production.getLooseFiberQuantity() > 0) {
            Double currentLooseFiberStock = looseFiberStockService.getCurrentStock(production.getFiberType());
            if (currentLooseFiberStock < production.getLooseFiberQuantity()) {
                throw new RuntimeException("Cannot delete production: Insufficient " + 
                    production.getFiberType().getDisplayName() + " loose fiber stock to reverse. " +
                    "Current: " + currentLooseFiberStock + " kg, Required: " + production.getLooseFiberQuantity() + " kg");
            }
        }
    }

    public List<Production> getProductionsByDateAndShift(LocalDate date, ShiftType shift) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        // Use batch completion time
        return productionRepository.findByBatchCompletionTimeBetweenAndShift(startOfDay, endOfDay, shift);
    }

    public List<Production> getProductionsByDateRange(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime startOfDay = fromDate.atStartOfDay();
        LocalDateTime endOfDay = toDate.plusDays(1).atStartOfDay();

        // Use batch completion time
        return productionRepository.findByBatchCompletionTimeBetween(startOfDay, endOfDay);
    }

    public int calculateTotalPithUsed(List<Production> firstShift, List<Production> secondShift) {
        return calculateShiftPithUsed(firstShift) + calculateShiftPithUsed(secondShift);
    }

    private int calculateShiftPithUsed(List<Production> productions) {
        return productions.stream()
                .filter(CocopithProduction.class::isInstance)
                .map(CocopithProduction.class::cast)
                .mapToInt(p -> p.getPithQuantityUsed().intValue())
                .sum();
    }

    public int calculateTotalLowEcPithProduced(List<Production> firstShift, List<Production> secondShift) {
        return calculateShiftLowEcPithProduced(firstShift) + calculateShiftLowEcPithProduced(secondShift);
    }

    private int calculateShiftLowEcPithProduced(List<Production> productions) {
        return productions.stream()
                .filter(CocopithProduction.class::isInstance)
                .map(CocopithProduction.class::cast)
                .mapToInt(p -> p.getLowEcQuantityProduced().intValue())
                .sum();
    }

    public int calculateTotalBlocksProduced(List<Production> firstShift, List<Production> secondShift,
            PithType pithType) {
        return calculateShiftBlocksProduced(firstShift, pithType) + calculateShiftBlocksProduced(secondShift, pithType);
    }

    private int calculateShiftBlocksProduced(List<Production> productions, PithType pithType) {
        return productions.stream()
                .filter(BlockProduction.class::isInstance)
                .map(BlockProduction.class::cast)
                .filter(p -> p.getPithType() == pithType)
                .mapToInt(BlockProduction::getBlocksProduced)
                .sum();
    }
}