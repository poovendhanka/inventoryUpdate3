package com.inventory.service.impl;

import com.inventory.model.FiberStock;
import com.inventory.repository.FiberStockRepository;
import com.inventory.service.FibreStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FibreStockServiceImpl implements FibreStockService {
    
    private final FiberStockRepository fiberStockRepository;
    
    @Override
    @Transactional
    public void addStock(Double bales) {
        FiberStock currentStock = fiberStockRepository.findTopByOrderByUpdatedAtDesc()
            .orElse(new FiberStock(0.0));
        
        FiberStock newStock = new FiberStock();
        newStock.setQuantity(currentStock.getQuantity() + bales);
        newStock.setUpdatedAt(LocalDateTime.now());
        fiberStockRepository.save(newStock);
    }
    
    @Override
    public Double getCurrentStock() {
        return fiberStockRepository.findTopByOrderByUpdatedAtDesc()
            .map(FiberStock::getQuantity)
            .orElse(0.0);
    }
} 