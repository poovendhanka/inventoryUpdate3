package com.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;
    
    @NotNull(message = "Date of birth is required")
    @Column(nullable = false)
    private LocalDate dateOfBirth;
    
    @NotBlank(message = "Aadhar number is required")
    @Pattern(regexp = "\\d{12}", message = "Aadhar number must be 12 digits")
    @Column(nullable = false, unique = true)
    private String aadharNumber;
    
    @NotBlank(message = "Address is required")
    @Column(nullable = false, length = 500)
    private String address;
    
    @NotBlank(message = "Contact is required")
    @Pattern(regexp = "\\d{10}", message = "Contact number must be 10 digits")
    @Column(nullable = false)
    private String contact;
    
    @NotNull(message = "Gender is required")
    @Column(nullable = false)
    private String gender; // "MALE" or "FEMALE"
    
    // Constructors
    public Employee() {}
    
    public Employee(String name, LocalDate dateOfBirth, String aadharNumber, String address, String contact, String gender) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.aadharNumber = aadharNumber;
        this.address = address;
        this.contact = contact;
        this.gender = gender;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getAadharNumber() {
        return aadharNumber;
    }
    
    public void setAadharNumber(String aadharNumber) {
        this.aadharNumber = aadharNumber;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getContact() {
        return contact;
    }
    
    public void setContact(String contact) {
        this.contact = contact;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
} 