package com.inventory.service;

import com.inventory.model.BlockProduction;
import com.inventory.model.PithType;
import com.inventory.repository.BlockProductionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlockProductionService {
    
    private final BlockProductionRepository blockProductionRepository;
    private final PithStockService pithStockService;
    
    @Transactional
    public void deleteBlockProduction(Long id) {
        BlockProduction blockProduction = blockProductionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Block production not found"));
        
        // Reverse stock changes
        reverseStockChanges(blockProduction);
        
        blockProductionRepository.delete(blockProduction);
    }
    
    private void reverseStockChanges(BlockProduction blockProduction) {
        // Calculate pith quantity that was used
        Double pithQuantityUsed = blockProduction.getPithQuantityUsed();
        if (pithQuantityUsed == null) {
            pithQuantityUsed = blockProduction.getBlocksProduced() * 5.0;
        }
        
        // Restore pith stock based on pith type
        if (blockProduction.getPithType() == PithType.NORMAL) {
            // Add back normal pith stock
            pithStockService.addStock(pithQuantityUsed);
        } else {
            // Add back low EC pith stock
            pithStockService.addLowEcStock(pithQuantityUsed);
        }
    }
} 