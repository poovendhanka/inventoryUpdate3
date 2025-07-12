package com.inventory.service;

import com.inventory.model.FiberType;

public interface LooseFiberStockService {
    
    void addStock(Double quantity, FiberType fiberType);
    
    Double getCurrentStock(FiberType fiberType);
    
    void validateStock(Double quantity, FiberType fiberType);
} 