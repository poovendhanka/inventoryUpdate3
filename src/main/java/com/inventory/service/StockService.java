package com.inventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.inventory.model.FiberType;

@Service
@RequiredArgsConstructor
public class StockService {

    private final PithStockService pithStockService;
    private final FibreStockService fibreStockService;

    public Double getCurrentPithStock() {
        return pithStockService.getCurrentStock();
    }

    public Double getCurrentFiberStock() {
        // Sum up both white and brown fiber stocks
        return fibreStockService.getCurrentStock(FiberType.WHITE) +
                fibreStockService.getCurrentStock(FiberType.BROWN);
    }

    public Double getCurrentFiberStock(FiberType fiberType) {
        return fibreStockService.getCurrentStock(fiberType);
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
    public void reduceFiberStock(Double quantity, FiberType fiberType) {
        Double currentStock = fibreStockService.getCurrentStock(fiberType);
        if (currentStock < quantity) {
            throw new RuntimeException("Insufficient " + fiberType.getDisplayName() + " stock");
        }
        fibreStockService.addStock(-quantity, fiberType);
    }
}