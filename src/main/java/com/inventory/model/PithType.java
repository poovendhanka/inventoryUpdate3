package com.inventory.model;

public enum PithType {
    NORMAL("Normal EC"),
    LOW("Low EC");

    private final String displayName;

    PithType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}