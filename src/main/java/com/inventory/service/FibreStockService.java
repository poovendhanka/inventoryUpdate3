package com.inventory.service;

import com.inventory.model.FiberType;

public interface FibreStockService {
    void addStock(Double bales, FiberType fiberType);

    Double getCurrentStock(FiberType fiberType);

}