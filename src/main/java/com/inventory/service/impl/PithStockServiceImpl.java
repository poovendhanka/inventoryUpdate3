package com.inventory.service.impl;

import com.inventory.model.PithStock;
import com.inventory.repository.PithStockRepository;
import com.inventory.service.PithStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import com.inventory.model.LowEcPithStock;
import com.inventory.repository.LowEcPithStockRepository;

@Service
@RequiredArgsConstructor
public class PithStockServiceImpl implements PithStockService {

    private final PithStockRepository pithStockRepository;
    private final LowEcPithStockRepository lowEcPithStockRepository;

    @Override
    @Transactional
    public void addStock(Double quantity) {
        PithStock currentStock = pithStockRepository.findTopByOrderByUpdatedAtDesc()
                .orElse(new PithStock(0.0));

        PithStock newStock = new PithStock();
        newStock.setQuantity(currentStock.getQuantity() + quantity);
        newStock.setUpdatedAt(LocalDateTime.now());
        pithStockRepository.save(newStock);
    }

    @Override
    public Double getCurrentStock() {
        return pithStockRepository.findTopByOrderByUpdatedAtDesc()
                .map(PithStock::getQuantity)
                .orElse(0.0);
    }

    @Override
    public Double getCurrentLowEcStock() {
        return lowEcPithStockRepository.findTopByOrderByUpdatedAtDesc()
                .map(LowEcPithStock::getQuantity)
                .orElse(0.0);
    }

    @Override
    @Transactional
    public void addLowEcStock(Double quantity) {
        LowEcPithStock currentStock = lowEcPithStockRepository.findTopByOrderByUpdatedAtDesc()
                .orElse(new LowEcPithStock(0.0));
        LowEcPithStock newStock = new LowEcPithStock(currentStock.getQuantity() + quantity);
        lowEcPithStockRepository.save(newStock);
    }
}