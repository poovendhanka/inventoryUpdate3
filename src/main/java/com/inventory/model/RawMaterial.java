package com.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "raw_materials")
public class RawMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receipt_number", unique = true)
    private String receiptNumber;

    @Column(name = "lorry_in_time")
    private LocalDateTime lorryInTime;

    @Column(name = "vehicle_number")
    private String vehicleNumber;

    @ManyToOne
    @JoinColumn(name = "party_id")
    private Party party;

    private Double length;
    private Double breadth;
    private Double height;
    private Double cft;

    @Column(name = "supervisor_name")
    private String supervisorName;

    @Column(name = "accounts_processed")
    private Boolean accountsProcessed = false;

    @OneToOne(mappedBy = "rawMaterial")
    private ProcessedRawMaterial processedDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "husk_type")
    private HuskType huskType;

    @PrePersist
    public void calculateCFT() {
        if (length != null && breadth != null && height != null) {
            this.cft = length * breadth * height;
        }
    }
}