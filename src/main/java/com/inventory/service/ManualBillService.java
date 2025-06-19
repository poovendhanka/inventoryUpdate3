package com.inventory.service;

import com.inventory.config.CompanyInfoProperties;
import com.inventory.model.ManualBill;
import com.inventory.model.ManualBillItem;
import com.inventory.repository.ManualBillRepository;
import com.inventory.repository.ManualBillItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ManualBillService {
    
    @Autowired
    private ManualBillRepository manualBillRepository;
    
    @Autowired
    private ManualBillItemRepository manualBillItemRepository;
    
    @Autowired
    private CompanyInfoProperties companyInfo;
    
    public String generateNextBillNumber() {
        Integer lastNumber = manualBillRepository.findLastBillNumber();
        return "MB" + String.format("%04d", (lastNumber != null ? lastNumber : 0) + 1);
    }
    
    public ManualBill createBill(ManualBill bill, List<ManualBillItem> items) {
        // Set bill number if not provided
        if (bill.getBillNumber() == null || bill.getBillNumber().isEmpty()) {
            bill.setBillNumber(generateNextBillNumber());
        }
        
        // Set bill date if not provided
        if (bill.getBillDate() == null) {
            bill.setBillDate(LocalDate.now());
        }
        
        // Calculate totals
        calculateBillTotals(bill, items);
        
        // Save bill
        ManualBill savedBill = manualBillRepository.save(bill);
        
        // Save items
        for (ManualBillItem item : items) {
            item.setManualBill(savedBill);
            manualBillItemRepository.save(item);
        }
        
        return savedBill;
    }
    
    public Optional<ManualBill> getBillById(Long id) {
        return manualBillRepository.findById(id);
    }
    
    public Optional<ManualBill> getBillByNumber(String billNumber) {
        return manualBillRepository.findByBillNumber(billNumber);
    }
    
    public List<ManualBill> getAllBills() {
        return manualBillRepository.findAllOrderByBillDateDesc();
    }
    
    public List<ManualBill> getBillsByDateRange(LocalDate startDate, LocalDate endDate) {
        return manualBillRepository.findByBillDateBetween(startDate, endDate);
    }
    
    public List<ManualBill> searchBillsByCustomer(String customerName) {
        return manualBillRepository.findByCustomerNameContainingIgnoreCase(customerName);
    }
    
    public List<ManualBillItem> getBillItems(Long billId) {
        return manualBillItemRepository.findByManualBillId(billId);
    }
    

    
    public void deleteBill(Long billId) {
        manualBillItemRepository.deleteByManualBillId(billId);
        manualBillRepository.deleteById(billId);
    }
    
    private void calculateBillTotals(ManualBill bill, List<ManualBillItem> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;
        
        for (ManualBillItem item : items) {
            // Calculate base amount (before tax)
            BigDecimal baseAmount = item.getQuantity().multiply(item.getRate());
            subtotal = subtotal.add(baseAmount);  // Subtotal is before tax
            
            // Calculate tax amounts
            BigDecimal itemCgst = BigDecimal.ZERO;
            BigDecimal itemSgst = BigDecimal.ZERO;
            BigDecimal itemIgst = BigDecimal.ZERO;
            
            if (item.getCgstRate() != null && item.getCgstRate().compareTo(BigDecimal.ZERO) > 0) {
                itemCgst = baseAmount.multiply(item.getCgstRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                totalCgst = totalCgst.add(itemCgst);
            }
            
            if (item.getSgstRate() != null && item.getSgstRate().compareTo(BigDecimal.ZERO) > 0) {
                itemSgst = baseAmount.multiply(item.getSgstRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                totalSgst = totalSgst.add(itemSgst);
            }
            
            if (item.getIgstRate() != null && item.getIgstRate().compareTo(BigDecimal.ZERO) > 0) {
                itemIgst = baseAmount.multiply(item.getIgstRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                totalIgst = totalIgst.add(itemIgst);
            }
            
            // Set item amount to total including tax (to match reference bill format)
            BigDecimal itemTotalWithTax = baseAmount.add(itemCgst).add(itemSgst).add(itemIgst);
            item.setAmount(itemTotalWithTax);
        }
        
        bill.setSubtotal(subtotal);
        bill.setCgstAmount(totalCgst);
        bill.setSgstAmount(totalSgst);
        bill.setIgstAmount(totalIgst);
        bill.setTotalAmount(subtotal.add(totalCgst).add(totalSgst).add(totalIgst));
    }
    
    public CompanyInfoProperties getCompanyInfo() {
        return companyInfo;
    }
} 