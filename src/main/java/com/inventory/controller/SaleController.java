package com.inventory.controller;

import com.inventory.model.Sale;
import com.inventory.model.ProductType;
import com.inventory.service.SaleService;
import com.inventory.service.DealerService;
import com.inventory.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Controller
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SaleController extends BaseController {
    
    private final SaleService saleService;
    private final DealerService dealerService;
    private final StockService stockService;
    
    @GetMapping
    public String showSalesPage(Model model, HttpServletRequest request) {
        model.addAttribute("activeTab", "sales");
        model.addAttribute("sale", new Sale());
        model.addAttribute("dealers", dealerService.getAllDealers());
        model.addAttribute("productTypes", ProductType.values());
        model.addAttribute("currentPithStock", stockService.getCurrentPithStock());
        model.addAttribute("currentFiberStock", stockService.getCurrentFiberStock());
        model.addAttribute("recentSales", saleService.getRecentSales());
        return getViewPath("sales/index");
    }
    
    @PostMapping
    public String createSale(@ModelAttribute Sale sale) {
        sale.setSaleDate(LocalDateTime.now());
        saleService.createSale(sale);
        return "redirect:/sales";
    }
    
    @GetMapping("/report")
    public String viewReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            Model model) {
        model.addAttribute("activeTab", "sales");
        model.addAttribute("date", date);
        model.addAttribute("sales", saleService.getSalesByDate(date));
        return getViewPath("sales/report");
    }
} 