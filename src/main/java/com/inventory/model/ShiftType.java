package com.inventory.model;

public enum ShiftType {
    FIRST("First Shift (8:00 AM - 8:00 PM)"),
    SECOND("Second Shift (8:00 PM - 8:00 AM)");
    
    private final String description;
    
    ShiftType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
} 