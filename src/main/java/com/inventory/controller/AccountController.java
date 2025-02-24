package com.inventory.controller;

import com.inventory.model.*;
import com.inventory.service.AccountService;
import com.inventory.service.RawMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController extends BaseController {
    private final AccountService accountService;
    private final RawMaterialService rawMaterialService;
    
    @GetMapping
    public String showAccountsPage(Model model, HttpServletRequest request) {
        model.addAttribute("activeTab", "accounts");
        model.addAttribute("unprocessedEntries", rawMaterialService.getUnprocessedEntries());
        return getViewPath("accounts/index");
    }
    
    @PostMapping("/process")
    public String processRawMaterial(@RequestParam Long rawMaterialId,
                                   @RequestParam Double cost,
                                   @RequestParam String supervisorName) {
        log.info("Processing raw material ID: {} with cost: {} and supervisor: {}", 
                rawMaterialId, cost, supervisorName);
                
        try {
            ProcessedRawMaterial processed = rawMaterialService.processRawMaterial(rawMaterialId, cost, supervisorName);
            log.info("Successfully processed raw material. ProcessedRawMaterial ID: {}", processed.getId());
            return "redirect:/accounts";
        } catch (Exception e) {
            log.error("Error processing raw material: {}", e.getMessage(), e);
            // You might want to add error handling here
            return "redirect:/accounts?error=" + e.getMessage();
        }
    }
    
    @PostMapping("/expenses")
    public String recordExpense(@ModelAttribute Expense expense) {
        accountService.recordExpense(expense);
        return "redirect:/accounts";
    }
    
    @GetMapping("/report")
    public String showAccountsReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) TransactionType type,
            Model model) {
        if (type != null) {
            model.addAttribute("transactions", 
                accountService.getTransactionsByTypeAndDateRange(type, startDate, endDate));
        } else {
            model.addAttribute("transactions", 
                accountService.getTransactionsByDateRange(startDate, endDate));
        }
        model.addAttribute("profitLoss", 
            accountService.calculateProfitLoss(startDate, endDate));
        return "accounts/report";
    }
} 