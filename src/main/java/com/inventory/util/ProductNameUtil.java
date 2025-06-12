package com.inventory.util;

import com.inventory.model.ProductType;
import com.inventory.model.PithType;
import com.inventory.model.FiberType;

public class ProductNameUtil {

    public static String getFullProductName(ProductType productType, PithType pithType, FiberType fiberType) {
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
}