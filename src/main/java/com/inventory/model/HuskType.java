package com.inventory.model;

public enum HuskType {
    BROWN("Brown Husk"),
    GREEN("Green Husk");

    private final String displayName;

    HuskType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}