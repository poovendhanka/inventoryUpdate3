package com.inventory.service.impl;

import com.inventory.model.HuskStock;
import com.inventory.model.HuskType;
import com.inventory.repository.HuskStockRepository;
import com.inventory.service.HuskStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HuskStockServiceImpl implements HuskStockService {

    private final HuskStockRepository huskStockRepository;

    @Override
    @Transactional
    public void addStock(HuskType huskType, Double quantity) {
        HuskStock currentStock = huskStockRepository.findTopByHuskTypeOrderByUpdatedAtDesc(huskType)
                .orElse(new HuskStock(huskType, 0.0));
        HuskStock newStock = new HuskStock();
        newStock.setHuskType(huskType);
        newStock.setQuantity(currentStock.getQuantity() + quantity);
        newStock.setUpdatedAt(LocalDateTime.now());
        huskStockRepository.save(newStock);
    }

    @Override
    public Double getCurrentStock(HuskType huskType) {
        return huskStockRepository.findTopByHuskTypeOrderByUpdatedAtDesc(huskType)
                .map(HuskStock::getQuantity)
                .orElse(0.0);
    }
} 