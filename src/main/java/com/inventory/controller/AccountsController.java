package com.inventory.controller;

import com.inventory.service.RawMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/accounts-report")
@RequiredArgsConstructor
public class AccountsController extends BaseController {
    private final RawMaterialService rawMaterialService;
    
    @GetMapping
    public String showAccountsPage(Model model) {
        model.addAttribute("activeTab", "accounts");
        return getViewPath("accounts/index");
    }
    
    @GetMapping("/raw-materials")
    public String showProcessedReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            Model model) {
        
        if (date == null) {
            date = LocalDate.now();
        }
        
        model.addAttribute("activeTab", "accounts");
        model.addAttribute("report", rawMaterialService.getProcessedReport(date));
        model.addAttribute("selectedDate", date);
        return getViewPath("accounts/raw-materials");
    }
} 