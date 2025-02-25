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
        return productionRepository.findByProductionDateAndShiftOrderByBatchCompletionTimeAsc(date, shift);
    }
}