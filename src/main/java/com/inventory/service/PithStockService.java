package com.inventory.service;

public interface PithStockService {
    void addStock(Double quantity);

    Double getCurrentStock();

    Double getCurrentLowEcStock();

    void addLowEcStock(Double quantity);
}