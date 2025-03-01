package com.inventory.model;

public enum HuskType {
    GREEN("Green Husk"),
    BROWN("Brown Husk");

    private final String displayName;

    HuskType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}