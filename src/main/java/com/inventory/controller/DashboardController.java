package com.inventory.controller;

import com.inventory.service.StockService;
import com.inventory.service.LooseFiberStockService;
import com.inventory.model.FiberType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping({ "/", "/dashboard" })
@RequiredArgsConstructor
public class DashboardController extends BaseController {
    private final StockService stockService;
    private final LooseFiberStockService looseFiberStockService;

    @GetMapping
    public String showDashboard(Model model, HttpServletRequest request) {
        model.addAttribute("activeTab", "dashboard");
        model.addAttribute("currentPithStock", stockService.getCurrentPithStock());
        model.addAttribute("currentLowEcPithStock", stockService.getCurrentLowEcPithStock());
        
        // Baled fiber stocks
        model.addAttribute("whiteFiberBaleStock", stockService.getCurrentFiberStock(FiberType.WHITE));
        model.addAttribute("brownFiberBaleStock", stockService.getCurrentFiberStock(FiberType.BROWN));
        model.addAttribute("totalFiberBaleStock", stockService.getCurrentFiberStock());
        model.addAttribute("currentFiberStock", stockService.getCurrentFiberStock()); // For backward compatibility
        
        // Loose fiber stocks
        double whiteLooseFiberStock = looseFiberStockService.getCurrentStock(FiberType.WHITE);
        double brownLooseFiberStock = looseFiberStockService.getCurrentStock(FiberType.BROWN);
        model.addAttribute("whiteLooseFiberStock", whiteLooseFiberStock);
        model.addAttribute("brownLooseFiberStock", brownLooseFiberStock);
        model.addAttribute("totalLooseFiberStock", whiteLooseFiberStock + brownLooseFiberStock);
        model.addAttribute("currentLooseFiberStock", whiteLooseFiberStock + brownLooseFiberStock); // For backward compatibility

        return getViewPath("dashboard/index");
    }
}