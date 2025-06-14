package com.inventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.inventory.model.FiberType;
import com.inventory.model.CocopithProduction;
import com.inventory.model.BlockProduction;
import com.inventory.model.PithType;
import com.inventory.repository.CocopithProductionRepository;
import com.inventory.repository.BlockProductionRepository;
import com.inventory.repository.SaleRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.time.Duration;
import com.inventory.model.HuskType;
import com.inventory.service.HuskStockService;

@Service
@RequiredArgsConstructor
public class StockService {

    private final PithStockService pithStockService;
    private final FibreStockService fibreStockService;
    private final CocopithProductionRepository cocopithProductionRepository;
    private final BlockProductionRepository blockProductionRepository;
    private final SaleRepository saleRepository;
    private final HuskStockService huskStockService;

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
    public void convertToLowEcPith(Double quantity, String supervisorName, Duration productionTime,
            LocalDateTime productionStartTime) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        // Check if we have enough pith stock
        if (pithStockService.getCurrentStock() < quantity) {
            throw new IllegalArgumentException("Not enough pith stock available");
        }

        // Reduce from pith stock
        pithStockService.addStock(-quantity);

        // Add to Low EC pith stock
        pithStockService.addLowEcStock(quantity);

        // Create cocopith production record
        CocopithProduction production = new CocopithProduction();
        production.setPithQuantityUsed(quantity);
        production.setLowEcQuantityProduced(quantity);
        production.setSupervisorName(supervisorName);
        production.setProductionTime(productionTime.abs()); // Ensure duration is positive
        production.setProductionStartTime(productionStartTime);
        production.setSystemTime(LocalDateTime.now());
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

    @Transactional
    public void reduceLowEcPithStock(Double quantity) {
        Double currentStock = getCurrentLowEcPithStock();
        if (currentStock < quantity) {
            throw new RuntimeException("Insufficient Low EC pith stock");
        }
        pithStockService.addLowEcStock(-quantity);
    }

    @Transactional
    public void reduceBlockStock(Integer blockCount) {
        // Each block uses 5kg of pith
        Double pithQuantityNeeded = blockCount * 5.0;
        Double currentStock = getCurrentPithStock();
        if (currentStock < pithQuantityNeeded) {
            throw new RuntimeException("Insufficient pith stock for block sales");
        }
        pithStockService.addStock(-pithQuantityNeeded);
    }

    public Integer getCurrentBlockStock(PithType pithType) {
        Integer totalProduced = blockProductionRepository.getTotalBlocksProducedByType(pithType);
        Integer totalSold = saleRepository.getTotalBlocksSoldByType(pithType);
        return (totalProduced != null ? totalProduced : 0) - (totalSold != null ? totalSold : 0);
    }

    public Integer getCurrentBlockStock() {
        return getCurrentBlockStock(PithType.NORMAL) + getCurrentBlockStock(PithType.LOW);
    }

    public Double getCurrentHuskStock(HuskType huskType) {
        return huskStockService.getCurrentStock(huskType);
    }

    @Transactional
    public void addHuskStock(HuskType huskType, Double quantity) {
        huskStockService.addStock(huskType, quantity);
    }

    @Transactional
    public void reduceHuskStock(HuskType huskType, Double quantity) {
        Double currentStock = getCurrentHuskStock(huskType);
        if (currentStock < quantity) {
            throw new RuntimeException("Insufficient " + huskType.getDisplayName() + " stock");
        }
        huskStockService.addStock(huskType, -quantity);
    }

    // Add methods for reversing stock changes (used when deleting entries)
    @Transactional
    public void addPithStock(Double quantity) {
        pithStockService.addStock(quantity);
    }

    @Transactional
    public void addLowEcPithStock(Double quantity) {
        pithStockService.addLowEcStock(quantity);
    }

    @Transactional
    public void addFiberStock(Double quantity, FiberType fiberType) {
        fibreStockService.addStock(quantity, fiberType);
    }

    @Transactional
    public void addBlockStock(Integer blockCount) {
        // When adding blocks back, we need to add back the pith that was used
        Double pithQuantityToAdd = blockCount * 5.0;
        pithStockService.addStock(pithQuantityToAdd);
    }
}