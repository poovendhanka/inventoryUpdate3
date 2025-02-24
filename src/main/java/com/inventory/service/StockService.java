package com.inventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {
    
    private final PithStockService pithStockService;
    private final FibreStockService fibreStockService;
    
    public Double getCurrentPithStock() {
        return pithStockService.getCurrentStock();
    }
    
    public Double getCurrentFiberStock() {
        return fibreStockService.getCurrentStock();
    }
    
    @Transactional
    public void reducePithStock(Double quantity) {
        Double currentStock = getCurrentPithStock();
        if (currentStock < quantity) {
            throw new RuntimeException("Insufficient pith stock");
        }
        pithStockService.addStock(-quantity);
    }
    
    @Transactional
    public void reduceFiberStock(Double quantity) {
        Double currentStock = getCurrentFiberStock();
        if (currentStock < quantity) {
            throw new RuntimeException("Insufficient fiber stock");
        }
        fibreStockService.addStock(-quantity);
    }
} 