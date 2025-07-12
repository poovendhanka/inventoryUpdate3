package com.inventory.service;

import com.inventory.model.FiberType;
import com.inventory.model.FiberProduction;
import com.inventory.repository.FiberProductionRepository;
import com.inventory.service.LooseFiberStockService;
import com.inventory.service.FibreStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FiberProductionService {

    private final LooseFiberStockService looseFiberStockService;
    private final FibreStockService fibreStockService;
    private final FiberProductionRepository fiberProductionRepository;
    
    private static final double BALE_WEIGHT_KG = 35.0; // 35kg per bale

    @Transactional
    public void convertLooseFiberToBales(FiberType fiberType, Integer numberOfBales, String supervisorName) {
        double requiredFiberKg = numberOfBales * BALE_WEIGHT_KG;
        
        // Validate sufficient loose fiber stock
        looseFiberStockService.validateStock(requiredFiberKg, fiberType);
        
        // Reduce loose fiber stock
        looseFiberStockService.addStock(-requiredFiberKg, fiberType);
        
        // Add to baled fiber stock
        fibreStockService.addStock(numberOfBales.doubleValue(), fiberType);
        
        // Record the fiber production
        FiberProduction fiberProduction = new FiberProduction();
        fiberProduction.setFiberType(fiberType);
        fiberProduction.setLooseFiberConsumed(requiredFiberKg);
        fiberProduction.setBalesProduced(numberOfBales);
        fiberProduction.setSupervisorName(supervisorName);
        fiberProductionRepository.save(fiberProduction);
    }
    
    public double calculateRequiredLooseFiber(Integer numberOfBales) {
        return numberOfBales * BALE_WEIGHT_KG;
    }
    
    public boolean canProduceBales(FiberType fiberType, Integer numberOfBales) {
        double requiredFiberKg = numberOfBales * BALE_WEIGHT_KG;
        double availableStock = looseFiberStockService.getCurrentStock(fiberType);
        return availableStock >= requiredFiberKg;
    }
    
    @Transactional
    public void deleteFiberProduction(Long id) {
        FiberProduction fiberProduction = fiberProductionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fiber production not found"));
        
        // Reverse stock changes
        reverseStockChanges(fiberProduction);
        
        fiberProductionRepository.delete(fiberProduction);
    }
    
    private void reverseStockChanges(FiberProduction fiberProduction) {
        // Validate stock availability before reversal
        validateStockForDeletion(fiberProduction);
        
        // Reverse baled fiber stock addition (reduce it)
        if (fiberProduction.getBalesProduced() != null && fiberProduction.getBalesProduced() > 0) {
            fibreStockService.addStock(-fiberProduction.getBalesProduced().doubleValue(), fiberProduction.getFiberType());
        }
        
        // Reverse loose fiber stock reduction (add it back)
        if (fiberProduction.getLooseFiberConsumed() != null && fiberProduction.getLooseFiberConsumed() > 0) {
            looseFiberStockService.addStock(fiberProduction.getLooseFiberConsumed(), fiberProduction.getFiberType());
        }
    }
    
    private void validateStockForDeletion(FiberProduction fiberProduction) {
        // Check if we have enough baled fiber stock to reduce
        if (fiberProduction.getBalesProduced() != null && fiberProduction.getBalesProduced() > 0) {
            Double currentBaleStock = fibreStockService.getCurrentStock(fiberProduction.getFiberType());
            if (currentBaleStock < fiberProduction.getBalesProduced()) {
                throw new RuntimeException("Cannot delete fiber production: Insufficient " + 
                    fiberProduction.getFiberType().getDisplayName() + " baled fiber stock to reverse. " +
                    "Current: " + currentBaleStock + " bales, Required: " + fiberProduction.getBalesProduced() + " bales");
            }
        }
    }
} 