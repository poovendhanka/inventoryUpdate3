package com.inventory.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "parties")
public class Party {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String sinNumber;
    private String phoneNumber;
} 