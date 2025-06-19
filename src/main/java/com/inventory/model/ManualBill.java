package com.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "manual_bills")
public class ManualBill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "bill_number", unique = true, nullable = false)
    private String billNumber;
    
    @Column(name = "bill_date", nullable = false)
    private LocalDate billDate;
    
    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
    @Column(name = "customer_name", nullable = false)
    private String customerName;
    
    @NotBlank(message = "Customer address is required")
    @Size(min = 10, max = 500, message = "Customer address must be between 10 and 500 characters")
    @Column(name = "customer_address", columnDefinition = "TEXT")
    private String customerAddress;
    
    @NotBlank(message = "Customer GST number is required")
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$", 
             message = "Invalid GST number format (e.g., 22AAAAA0000A1Z5)")
    @Column(name = "customer_gst")
    private String customerGst;
    
    @NotBlank(message = "Customer mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", 
             message = "Mobile number must be 10 digits starting with 6-9")
    @Column(name = "customer_mobile")
    private String customerMobile;
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    @Column(name = "customer_email")
    private String customerEmail;
    
    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;
    
    @Column(name = "same_as_billing", nullable = false)
    private Boolean sameAsBilling = true;
    
    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @Column(name = "cgst_amount", precision = 10, scale = 2)
    private BigDecimal cgstAmount;
    
    @Column(name = "sgst_amount", precision = 10, scale = 2)
    private BigDecimal sgstAmount;
    
    @Column(name = "igst_amount", precision = 10, scale = 2)
    private BigDecimal igstAmount;
    
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @OneToMany(mappedBy = "manualBill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ManualBillItem> items;
    
    // Constructors
    public ManualBill() {}
    
    public ManualBill(String billNumber, LocalDate billDate, String customerName) {
        this.billNumber = billNumber;
        this.billDate = billDate;
        this.customerName = customerName;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getBillNumber() {
        return billNumber;
    }
    
    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }
    
    public LocalDate getBillDate() {
        return billDate;
    }
    
    public void setBillDate(LocalDate billDate) {
        this.billDate = billDate;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getCustomerAddress() {
        return customerAddress;
    }
    
    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }
    
    public String getCustomerGst() {
        return customerGst;
    }
    
    public void setCustomerGst(String customerGst) {
        this.customerGst = customerGst;
    }
    
    public String getCustomerMobile() {
        return customerMobile;
    }
    
    public void setCustomerMobile(String customerMobile) {
        this.customerMobile = customerMobile;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    
    public String getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    
    public Boolean getSameAsBilling() {
        return sameAsBilling;
    }
    
    public void setSameAsBilling(Boolean sameAsBilling) {
        this.sameAsBilling = sameAsBilling;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public BigDecimal getCgstAmount() {
        return cgstAmount;
    }
    
    public void setCgstAmount(BigDecimal cgstAmount) {
        this.cgstAmount = cgstAmount;
    }
    
    public BigDecimal getSgstAmount() {
        return sgstAmount;
    }
    
    public void setSgstAmount(BigDecimal sgstAmount) {
        this.sgstAmount = sgstAmount;
    }
    
    public BigDecimal getIgstAmount() {
        return igstAmount;
    }
    
    public void setIgstAmount(BigDecimal igstAmount) {
        this.igstAmount = igstAmount;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public List<ManualBillItem> getItems() {
        return items;
    }
    
    public void setItems(List<ManualBillItem> items) {
        this.items = items;
    }
} 