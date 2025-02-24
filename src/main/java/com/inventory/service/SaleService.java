package com.inventory.service;

import com.inventory.model.Sale;
import com.inventory.model.ProductType;
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
    public Sale createSale(Sale sale) {
        // Generate invoice number
        long count = saleRepository.count();
        String invoiceNumber = String.format("INV-%05d", count + 1);
        sale.setInvoiceNumber(invoiceNumber);
        
        // Reduce stock based on product type
        if (sale.getProductType() == ProductType.PITH) {
            stockService.reducePithStock(sale.getQuantity());
        } else {
            stockService.reduceFiberStock(sale.getQuantity());
        }
        
        return saleRepository.save(sale);
    }
    
    public List<Sale> getRecentSales() {
        return saleRepository.findTop10ByOrderBySaleDateDesc();
    }
    
    public List<Sale> getSalesByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        return saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(startOfDay, endOfDay);
    }
} 