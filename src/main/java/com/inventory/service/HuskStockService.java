package com.inventory.service;

import com.inventory.model.HuskType;

public interface HuskStockService {
    void addStock(HuskType huskType, Double quantity);
    Double getCurrentStock(HuskType huskType);
} 