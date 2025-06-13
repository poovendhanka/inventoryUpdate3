package com.inventory.util;

import com.inventory.model.ProductType;
import com.inventory.model.PithType;
import com.inventory.model.FiberType;

public class ProductNameUtil {

    public static String getFullProductName(ProductType productType, PithType pithType, FiberType fiberType) {
        if (productType == null) {
            return "Unknown Product";
        }
        
        switch (productType) {
            case PITH:
                return pithType == PithType.NORMAL ? "High EC Pith" : "Low EC Pith";
            case FIBER:
                return fiberType == FiberType.WHITE ? "White Fiber" : "Brown Fiber";
            case BLOCK:
                return pithType == PithType.NORMAL ? "High EC Block" : "Low EC Block";
            default:
                return "Unknown Product";
        }
    }
    
    // Overloaded method for when fiberType is not needed (PITH and BLOCK products)
    public static String getFullProductName(ProductType productType, PithType pithType) {
        return getFullProductName(productType, pithType, null);
    }
    
    // Overloaded method for when pithType is not needed (FIBER products)
    public static String getFullProductName(ProductType productType, FiberType fiberType) {
        return getFullProductName(productType, null, fiberType);
    }
}