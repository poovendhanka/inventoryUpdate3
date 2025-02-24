package com.inventory.controller;

import com.inventory.service.ProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import com.inventory.model.ShiftType;

@Controller
@RequestMapping("/production-report")
@RequiredArgsConstructor
public class ProductionReportController extends BaseController {
    
    private final ProductionService productionService;
    
    @GetMapping
    public String showReportPage(Model model, HttpServletRequest request) {
        model.addAttribute("activeTab", "production-report");
        return getViewPath("production-report/index");
    }
    
    @GetMapping("/view")
    public String viewReport(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date, Model model) {
        model.addAttribute("date", date);
        model.addAttribute("firstShift", productionService.getProductionsByDateAndShift(date, ShiftType.FIRST));
        model.addAttribute("secondShift", productionService.getProductionsByDateAndShift(date, ShiftType.SECOND));
        return getViewPath("production-report/view");
    }
} 