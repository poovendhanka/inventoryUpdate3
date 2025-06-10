package com.inventory.model;

public enum ExpenseType {
    ELECTRICITY_BILLS("Electricity/Bills"),
    LABOUR("Labour"),
    MAINTENANCE("Maintenance"),
    TRANSPORTATION("Transportation"),
    SPARES_PARTS("Spares/Parts"),
    MISCELLANEOUS("Miscellaneous");

    private final String displayName;

    ExpenseType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}