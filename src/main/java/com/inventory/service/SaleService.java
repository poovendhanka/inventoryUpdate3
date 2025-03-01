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
}