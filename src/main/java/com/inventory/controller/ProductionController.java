package com.inventory.controller;

import com.inventory.model.Production;
import com.inventory.model.ShiftType;
import com.inventory.service.ProductionService;
import com.inventory.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/production")
@RequiredArgsConstructor
public class ProductionController extends BaseController {
    
    private final ProductionService productionService;
    private final StockService stockService;
    
    @GetMapping
    public String showProductionPage(Model model, HttpServletRequest request) {
        model.addAttribute("activeTab", "production");
        model.addAttribute("production", new Production());
        model.addAttribute("shifts", ShiftType.values());
        model.addAttribute("currentPithStock", stockService.getCurrentPithStock());
        model.addAttribute("currentFiberStock", stockService.getCurrentFiberStock());
        
        // Add recent productions
        model.addAttribute("recentProductions", 
            productionService.getRecentProductions(LocalDate.now()));
        
        return getViewPath("production/index");
    }
    
    @PostMapping
    public String createProduction(@ModelAttribute Production production, RedirectAttributes redirectAttributes) {
        try {
            productionService.createProduction(production);
            redirectAttributes.addFlashAttribute("success", "Production batch completed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/production";
    }
    
    @GetMapping("/report")
    public String viewReport(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date, Model model) {
        model.addAttribute("date", date);
        model.addAttribute("firstShift", productionService.getProductionsByDateAndShift(date, ShiftType.FIRST));
        model.addAttribute("secondShift", productionService.getProductionsByDateAndShift(date, ShiftType.SECOND));
        return getViewPath("production-report/view");
    }
} 