package com.inventory.controller;

import com.inventory.model.FiberType;
import com.inventory.model.PithType;
import com.inventory.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/production-inventory")
@RequiredArgsConstructor
public class ProductionInventoryController extends BaseController {

    private final StockService stockService;
    private static final Logger log = LoggerFactory.getLogger(ProductionInventoryController.class);

    @GetMapping
    public String showProductionInventoryPage(Model model) {
        try {
            // Get current stock values
            double normalEcPithStock = stockService.getCurrentPithStock();
            double lowEcPithStock = stockService.getCurrentLowEcPithStock();
            double whiteFiberStock = stockService.getCurrentFiberStock(FiberType.WHITE);
            double brownFiberStock = stockService.getCurrentFiberStock(FiberType.BROWN);
            int blocks5kgStock = stockService.getCurrentBlockStock(PithType.NORMAL);
            int blocks650gStock = stockService.getCurrentBlockStock(PithType.LOW);

            // Add stock values to model
            model.addAttribute("normalEcPithStock", normalEcPithStock);
            model.addAttribute("lowEcPithStock", lowEcPithStock);
            model.addAttribute("whiteFiberStock", whiteFiberStock);
            model.addAttribute("brownFiberStock", brownFiberStock);
            model.addAttribute("blocks5kgStock", blocks5kgStock);
            model.addAttribute("blocks650gStock", blocks650gStock);

            // Set active tab for sidebar highlighting
            model.addAttribute("activeTab", "production-inventory");

            return "production-inventory/index";
        } catch (Exception e) {
            log.error("Error loading production inventory page: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading production inventory page: " + e.getMessage());
            return "error";
        }
    }
}