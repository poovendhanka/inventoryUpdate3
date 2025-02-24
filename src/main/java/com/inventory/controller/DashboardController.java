package com.inventory.controller;

import com.inventory.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping({"/", "/dashboard"})
@RequiredArgsConstructor
public class DashboardController extends BaseController {
    private final StockService stockService;
    
    @GetMapping
    public String showDashboard(Model model, HttpServletRequest request) {
        model.addAttribute("activeTab", "dashboard");
        model.addAttribute("currentPithStock", stockService.getCurrentPithStock());
        model.addAttribute("currentFiberStock", stockService.getCurrentFiberStock());
        
        return getViewPath("dashboard/index");
    }
} 