package com.inventory.model;

public enum ShiftType {
    FIRST("First Shift (8:00 AM - 8:00 PM)"),
    SECOND("Second Shift (8:00 PM - 8:00 AM)");
    
    private final String description;
    private final String displayName;
    
    ShiftType(String description) {
        this.description = description;
        // Extract a shorter display name from the description
        this.displayName = description.split(" \\(")[0];
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 