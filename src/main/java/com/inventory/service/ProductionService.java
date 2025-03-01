package com.inventory.service;

import com.inventory.model.Production;
import com.inventory.model.ShiftType;
import com.inventory.model.PithStock;
import com.inventory.model.FiberStock;
import com.inventory.model.BlockProduction;
import com.inventory.model.CocopithProduction;
import com.inventory.model.PithType;
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

@Service
@Transactional
@RequiredArgsConstructor
public class ProductionService {

    private final ProductionRepository productionRepository;
    private final PithStockService pithStockService;
    private final FibreStockService fibreStockService;

    public void createProduction(Production production) {
        // Generate batch number
        Integer batchNumber = generateBatchNumber();
        production.setBatchNumber(batchNumber);

        // Save production
        Production savedProduction = productionRepository.save(production);

        // Update stocks based on production
        updateStocks(savedProduction);
    }

    private void updateStocks(Production production) {
        // Update pith stock
        if (production.getPithQuantity() != null && production.getPithQuantity() > 0) {
            pithStockService.addStock(production.getPithQuantity());
        }

        // Update fiber stock
        if (production.getFiberBales() != null && production.getFiberBales() > 0) {
            fibreStockService.addStock(production.getFiberBales().doubleValue(), production.getFiberType());
        }
    }

    private Integer generateBatchNumber() {
        return (int) (productionRepository.count() + 1);
    }

    public List<Production> getRecentProductions() {
        return productionRepository.findTop10ByOrderByBatchCompletionTimeDesc();
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