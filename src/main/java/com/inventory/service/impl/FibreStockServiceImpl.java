package com.inventory.service.impl;

import com.inventory.model.WhiteFiberStock;
import com.inventory.model.BrownFiberStock;
import com.inventory.model.FiberType;
import com.inventory.repository.WhiteFiberStockRepository;
import com.inventory.repository.BrownFiberStockRepository;
import com.inventory.service.FibreStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FibreStockServiceImpl implements FibreStockService {

    private final WhiteFiberStockRepository whiteFiberStockRepository;
    private final BrownFiberStockRepository brownFiberStockRepository;

    @Override
    @Transactional
    public void addStock(Double bales, FiberType fiberType) {
        if (fiberType == FiberType.WHITE) {
            WhiteFiberStock currentStock = whiteFiberStockRepository.findTopByOrderByUpdatedAtDesc()
                    .orElse(new WhiteFiberStock(0.0));

            WhiteFiberStock newStock = new WhiteFiberStock(currentStock.getQuantity() + bales);
            whiteFiberStockRepository.save(newStock);
        } else {
            BrownFiberStock currentStock = brownFiberStockRepository.findTopByOrderByUpdatedAtDesc()
                    .orElse(new BrownFiberStock(0.0));

            BrownFiberStock newStock = new BrownFiberStock(currentStock.getQuantity() + bales);
            brownFiberStockRepository.save(newStock);
        }
    }

    @Override
    public Double getCurrentStock(FiberType fiberType) {
        if (fiberType == FiberType.WHITE) {
            return whiteFiberStockRepository.findTopByOrderByUpdatedAtDesc()
                    .map(WhiteFiberStock::getQuantity)
                    .orElse(0.0);
        } else {
            return brownFiberStockRepository.findTopByOrderByUpdatedAtDesc()
                    .map(BrownFiberStock::getQuantity)
                    .orElse(0.0);
        }
    }
}