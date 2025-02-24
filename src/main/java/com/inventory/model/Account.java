package com.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "accounts")
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "raw_material_id")
    private RawMaterial rawMaterial;
    
    private Double cost;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "supervisor_name")
    private String supervisorName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;
    
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;
    
    @Column(name = "reference_number")
    private String referenceNumber;
    
    private String description;
    
    @Column(name = "is_expense")
    private Boolean isExpense;
    
    private Double amount;
}