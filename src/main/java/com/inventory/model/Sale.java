package com.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sales")
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
    
    @Column(name = "sale_date")
    private LocalDateTime saleDate;
    
    @Column(name = "invoice_number")
    private String invoiceNumber;
    
    @PrePersist
    public void calculateTotal() {
        if (quantity != null && pricePerUnit != null) {
            this.totalAmount = quantity * pricePerUnit;
        }
    }
} 