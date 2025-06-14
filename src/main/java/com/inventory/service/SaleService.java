package com.inventory.service;

import com.inventory.model.Sale;
import com.inventory.model.ProductType;
import com.inventory.model.FiberType;
import com.inventory.model.PithType;
import com.inventory.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final StockService stockService;

    @Transactional
    public void createSale(Sale sale) {
        // Validate stock availability
        validateStockAvailability(sale);

        // Set block count if product type is BLOCK
        if (sale.getProductType() == ProductType.BLOCK) {
            sale.setBlockCount(sale.getQuantity().intValue());
        }

        // Save the sale
        saleRepository.save(sale);

        // Update stock based on product type
        updateStock(sale);
    }

    private void validateStockAvailability(Sale sale) {
        switch (sale.getProductType()) {
            case FIBER:
                double fiberStock = stockService.getCurrentFiberStock(sale.getFiberType());
                if (sale.getQuantity() > fiberStock) {
                    throw new IllegalStateException("Insufficient fiber stock");
                }
                break;
            case PITH:
                double pithStock = sale.getPithType() == PithType.NORMAL ? stockService.getCurrentPithStock()
                        : stockService.getCurrentLowEcPithStock();
                if (sale.getQuantity() > pithStock) {
                    throw new IllegalStateException("Insufficient pith stock");
                }
                break;
            case BLOCK:
                // Block validation can be added here if needed
                break;
        }
    }

    private void updateStock(Sale sale) {
        switch (sale.getProductType()) {
            case FIBER:
                stockService.reduceFiberStock(sale.getQuantity(), sale.getFiberType());
                break;
            case PITH:
                if (sale.getPithType() == PithType.NORMAL) {
                    stockService.reducePithStock(sale.getQuantity());
                } else {
                    stockService.reduceLowEcPithStock(sale.getQuantity());
                }
                break;
            case BLOCK:
                stockService.reduceBlockStock(sale.getBlockCount());
                break;
        }
    }

    public List<Sale> getRecentSales() {
        return saleRepository.findTop10ByOrderBySaleDateDesc();
    }

    public List<Sale> getSalesByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        return saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(startOfDay, endOfDay);
    }

    public List<Sale> getSalesByDateRange(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime startOfDay = fromDate.atStartOfDay();
        LocalDateTime endOfDay = toDate.plusDays(1).atStartOfDay();
        return saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(startOfDay, endOfDay);
    }

    public List<Sale> getSalesByDateRangeWithFilters(LocalDate fromDate, LocalDate toDate, String productType, Long dealerId) {
        LocalDateTime startOfDay = fromDate.atStartOfDay();
        LocalDateTime endOfDay = toDate.plusDays(1).atStartOfDay();
        
        List<Sale> sales = saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(startOfDay, endOfDay);
        
        // Apply product type filter
        if (productType != null && !productType.trim().isEmpty()) {
            sales = sales.stream()
                    .filter(sale -> matchesProductType(sale, productType))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Apply dealer filter
        if (dealerId != null) {
            sales = sales.stream()
                    .filter(sale -> sale.getDealer() != null && sale.getDealer().getId().equals(dealerId))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        return sales;
    }
    
    private boolean matchesProductType(Sale sale, String productType) {
        switch (productType) {
            case "HIGH_EC_PITH":
                return sale.getProductType() == ProductType.PITH && sale.getPithType() == PithType.NORMAL;
            case "LOW_EC_PITH":
                return sale.getProductType() == ProductType.PITH && sale.getPithType() == PithType.LOW;
            case "WHITE_FIBER":
                return sale.getProductType() == ProductType.FIBER && sale.getFiberType() == FiberType.WHITE;
            case "BROWN_FIBER":
                return sale.getProductType() == ProductType.FIBER && sale.getFiberType() == FiberType.BROWN;
            case "HIGH_EC_BLOCKS":
                return sale.getProductType() == ProductType.BLOCK && sale.getPithType() == PithType.NORMAL;
            case "LOW_EC_BLOCKS":
                return sale.getProductType() == ProductType.BLOCK && sale.getPithType() == PithType.LOW;
            default:
                return true; // No filter or unknown filter
        }
    }

    public Sale getSaleById(Long id) {
        return saleRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteSale(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
        
        // Reverse stock changes
        reverseStockChanges(sale);
        
        saleRepository.delete(sale);
    }

    private void reverseStockChanges(Sale sale) {
        // Add back the stock that was reduced during sale
        switch (sale.getProductType()) {
            case FIBER:
                stockService.addFiberStock(sale.getQuantity(), sale.getFiberType());
                break;
            case PITH:
                if (sale.getPithType() == PithType.NORMAL) {
                    stockService.addPithStock(sale.getQuantity());
                } else {
                    stockService.addLowEcPithStock(sale.getQuantity());
                }
                break;
            case BLOCK:
                stockService.addBlockStock(sale.getBlockCount());
                break;
        }
    }
}