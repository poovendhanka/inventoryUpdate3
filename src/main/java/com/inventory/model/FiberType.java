package com.inventory.model;

public enum FiberType {
    WHITE("White Fiber"),
    BROWN("Brown Fiber");

    private final String displayName;

    FiberType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}