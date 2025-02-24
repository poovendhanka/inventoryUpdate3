package com.inventory.model;

public enum ProductType {
    PITH("Pith"),
    FIBER("Fiber");
    
    private final String displayName;
    
    ProductType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 