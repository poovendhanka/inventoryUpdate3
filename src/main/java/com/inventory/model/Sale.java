package com.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sales")
@NoArgsConstructor
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type")
    private ProductType productType;

    private Double quantity;

    @Column(name = "price_per_unit")
    private Double pricePerUnit;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "cgst_amount")
    private Double cgstAmount;

    @Column(name = "sgst_amount")
    private Double sgstAmount;

    @Column(name = "tax_amount")
    private Double taxAmount;

    @Column(name = "total_with_tax")
    private Double totalWithTax;

    @Column(name = "sale_date")
    private LocalDateTime saleDate;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "fiber_type")
    private FiberType fiberType;

    @Enumerated(EnumType.STRING)
    @Column(name = "pith_type")
    private PithType pithType;

    @Column(name = "block_count")
    private Integer blockCount;

    @PrePersist
    @PreUpdate
    public void calculateTotal() {
        if (quantity != null && pricePerUnit != null) {
            this.totalAmount = quantity * pricePerUnit;
            
            // Initialize tax amounts to 0
            this.cgstAmount = 0.0;
            this.sgstAmount = 0.0;
            this.taxAmount = 0.0;
            
            // Calculate tax only for blocks (5% total = 2.5% CGST + 2.5% SGST)
            if (productType == ProductType.BLOCK) {
                this.cgstAmount = totalAmount * 2.5 / 100.0; // 2.5% CGST
                this.sgstAmount = totalAmount * 2.5 / 100.0; // 2.5% SGST
                this.taxAmount = cgstAmount + sgstAmount;
            }
            
            this.totalWithTax = totalAmount + taxAmount;
        }
    }
}