package com.inventory.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.math.BigDecimal;

@Entity
@Table(name = "manual_bill_items")
public class ManualBillItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manual_bill_id", nullable = false)
    @JsonBackReference
    private ManualBill manualBill;
    
    @Column(name = "item_description", nullable = false)
    private String itemDescription;
    
    @Column(name = "hsn_code")
    private String hsnCode;
    
    @Column(name = "quantity", precision = 10, scale = 2, nullable = false)
    private BigDecimal quantity;
    
    @Column(name = "unit")
    private String unit;
    
    @Column(name = "rate", precision = 10, scale = 2, nullable = false)
    private BigDecimal rate;
    
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "cgst_rate", precision = 5, scale = 2)
    private BigDecimal cgstRate;
    
    @Column(name = "sgst_rate", precision = 5, scale = 2)
    private BigDecimal sgstRate;
    
    @Column(name = "igst_rate", precision = 5, scale = 2)
    private BigDecimal igstRate;
    
    @Column(name = "tax_type")
    private String taxType; // "GST" for intra-state, "IGST" for inter-state
    
    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate; // The total tax rate entered by user
    
    // Constructors
    public ManualBillItem() {}
    
    public ManualBillItem(ManualBill manualBill, String itemDescription, BigDecimal quantity, BigDecimal rate) {
        this.manualBill = manualBill;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.rate = rate;
        this.amount = quantity.multiply(rate);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public ManualBill getManualBill() {
        return manualBill;
    }
    
    public void setManualBill(ManualBill manualBill) {
        this.manualBill = manualBill;
    }
    
    public String getItemDescription() {
        return itemDescription;
    }
    
    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }
    
    public String getHsnCode() {
        return hsnCode;
    }
    
    public void setHsnCode(String hsnCode) {
        this.hsnCode = hsnCode;
    }
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        calculateAmount();
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public BigDecimal getRate() {
        return rate;
    }
    
    public void setRate(BigDecimal rate) {
        this.rate = rate;
        calculateAmount();
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public BigDecimal getCgstRate() {
        return cgstRate;
    }
    
    public void setCgstRate(BigDecimal cgstRate) {
        this.cgstRate = cgstRate;
    }
    
    public BigDecimal getSgstRate() {
        return sgstRate;
    }
    
    public void setSgstRate(BigDecimal sgstRate) {
        this.sgstRate = sgstRate;
    }
    
    public BigDecimal getIgstRate() {
        return igstRate;
    }
    
    public void setIgstRate(BigDecimal igstRate) {
        this.igstRate = igstRate;
    }
    
    public String getTaxType() {
        return taxType;
    }
    
    public void setTaxType(String taxType) {
        this.taxType = taxType;
        // Automatically calculate CGST/SGST or IGST based on tax type
        if (taxRate != null) {
            calculateTaxRates();
        }
    }
    
    public BigDecimal getTaxRate() {
        return taxRate;
    }
    
    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
        // Automatically calculate CGST/SGST or IGST based on tax type
        if (taxType != null) {
            calculateTaxRates();
        }
    }
    
    private void calculateAmount() {
        if (quantity != null && rate != null) {
            this.amount = quantity.multiply(rate);
        }
    }
    
    private void calculateTaxRates() {
        if (taxRate == null) {
            this.cgstRate = BigDecimal.ZERO;
            this.sgstRate = BigDecimal.ZERO;
            this.igstRate = BigDecimal.ZERO;
            return;
        }
        
        if ("GST".equals(taxType)) {
            // Split GST equally into CGST and SGST
            BigDecimal halfRate = taxRate.divide(BigDecimal.valueOf(2), 2, java.math.RoundingMode.HALF_UP);
            this.cgstRate = halfRate;
            this.sgstRate = halfRate;
            this.igstRate = BigDecimal.ZERO;
        } else if ("IGST".equals(taxType)) {
            // Apply full rate as IGST
            this.cgstRate = BigDecimal.ZERO;
            this.sgstRate = BigDecimal.ZERO;
            this.igstRate = taxRate;
        } else {
            // Default: no tax
            this.cgstRate = BigDecimal.ZERO;
            this.sgstRate = BigDecimal.ZERO;
            this.igstRate = BigDecimal.ZERO;
        }
    }
} 