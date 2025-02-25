package com.inventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.inventory.model.FiberType;
import com.inventory.model.CocopithProduction;
import com.inventory.model.BlockProduction;
import com.inventory.model.PithType;
import com.inventory.repository.CocopithProductionRepository;

@Service
@RequiredArgsConstructor
public class StockService {

    private final PithStockService pithStockService;
    private final FibreStockService fibreStockService;
    private final CocopithProductionRepository cocopithProductionRepository;

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

    public Double getCurrentLowEcPithStock() {
        return pithStockService.getCurrentLowEcStock();
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

    @Transactional
    public void convertToLowEcPith(Double quantity, String supervisorName) {
        Double currentStock = getCurrentPithStock();
        if (currentStock < quantity) {
            throw new RuntimeException("Insufficient pith stock for conversion");
        }

        // Reduce normal pith stock
        pithStockService.addStock(-quantity);

        // Add to Low EC pith stock
        pithStockService.addLowEcStock(quantity);

        // Create cocopith production record
        CocopithProduction production = new CocopithProduction();
        production.setPithQuantityUsed(quantity);
        production.setLowEcQuantityProduced(quantity);
        production.setSupervisorName(supervisorName);
        cocopithProductionRepository.save(production);
    }

    @Transactional
    public void processBlockProduction(BlockProduction blockProduction) {
        Double pithQuantity = blockProduction.getBlocksProduced() * 5.0;

        if (blockProduction.getPithType() == PithType.NORMAL) {
            if (getCurrentPithStock() < pithQuantity) {
                throw new RuntimeException("Insufficient normal pith stock for block production");
            }
            pithStockService.addStock(-pithQuantity);
        } else {
            if (getCurrentLowEcPithStock() < pithQuantity) {
                throw new RuntimeException("Insufficient low EC pith stock for block production");
            }
            pithStockService.addLowEcStock(-pithQuantity);
        }
    }
}