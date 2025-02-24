package com.inventory.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "dealers")
public class Dealer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String phoneNumber;
    private String address;
    private String gstNumber;
} 