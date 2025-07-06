package com.inventory.service;

import com.inventory.model.CocopithProduction;
import com.inventory.repository.CocopithProductionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CocopithProductionService {
    
    private final CocopithProductionRepository cocopithProductionRepository;
    private final PithStockService pithStockService;
    
    @Transactional
    public void deleteCocopithProduction(Long id) {
        CocopithProduction cocopithProduction = cocopithProductionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cocopith production not found"));
        
        // Reverse stock changes
        reverseStockChanges(cocopithProduction);
        
        cocopithProductionRepository.delete(cocopithProduction);
    }
    
    private void reverseStockChanges(CocopithProduction cocopithProduction) {
        // Restore normal pith stock (add back what was used)
        if (cocopithProduction.getPithQuantityUsed() != null && cocopithProduction.getPithQuantityUsed() > 0) {
            pithStockService.addStock(cocopithProduction.getPithQuantityUsed());
        }
        
        // Remove low EC pith stock (remove what was produced)
        if (cocopithProduction.getLowEcQuantityProduced() != null && cocopithProduction.getLowEcQuantityProduced() > 0) {
            pithStockService.addLowEcStock(-cocopithProduction.getLowEcQuantityProduced());
        }
    }
} 