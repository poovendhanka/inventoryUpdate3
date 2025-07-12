package com.inventory.service.impl;

import com.inventory.model.LooseFiberStock;
import com.inventory.model.FiberType;
import com.inventory.repository.LooseFiberStockRepository;
import com.inventory.service.LooseFiberStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LooseFiberStockServiceImpl implements LooseFiberStockService {

    private final LooseFiberStockRepository looseFiberStockRepository;

    @Override
    @Transactional
    public void addStock(Double quantity, FiberType fiberType) {
        LooseFiberStock currentStock = looseFiberStockRepository.findTopByFiberTypeOrderByUpdatedAtDesc(fiberType)
                .orElse(new LooseFiberStock(0.0, fiberType));

        LooseFiberStock newStock = new LooseFiberStock(currentStock.getQuantity() + quantity, fiberType);
        looseFiberStockRepository.save(newStock);
    }

    @Override
    public Double getCurrentStock(FiberType fiberType) {
        return looseFiberStockRepository.findTopByFiberTypeOrderByUpdatedAtDesc(fiberType)
                .map(LooseFiberStock::getQuantity)
                .orElse(0.0);
    }

    @Override
    public void validateStock(Double quantity, FiberType fiberType) {
        Double currentStock = getCurrentStock(fiberType);
        if (currentStock < quantity) {
            throw new IllegalArgumentException("Insufficient " + fiberType.getDisplayName() + 
                " loose fiber stock. Available: " + currentStock + " kg, Required: " + quantity + " kg");
        }
    }
} 