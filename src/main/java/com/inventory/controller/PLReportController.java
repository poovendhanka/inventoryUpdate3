package com.inventory.controller;

import com.inventory.service.ExpenseService;
import com.inventory.service.RawMaterialService;
import com.inventory.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/pl-report")
@RequiredArgsConstructor
public class PLReportController {
    private final ExpenseService expenseService;
    private final RawMaterialService rawMaterialService;
    private final SaleService saleService;

    @GetMapping
    public String showPLReport(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            Model model) {
        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null) endDate = LocalDate.now();

        // Purchases (raw material processed cost)
        double purchaseAmount = rawMaterialService.getProcessedReport(startDate, endDate).getTotalCost();
        // Expenses
        double expenses = expenseService.getTotalExpenseAmountByDateRange(startDate, endDate);
        // Sales Revenue (including tax as that's our total revenue)
        double revenue = saleService.getSalesByDateRange(startDate, endDate).stream().mapToDouble(s -> s.getTotalWithTax() != null ? s.getTotalWithTax() : 0.0).sum();
        // Profit & Loss
        double profit = revenue - (purchaseAmount + expenses);

        model.addAttribute("activeTab", "pl-report");
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("purchaseAmount", purchaseAmount);
        model.addAttribute("expenses", expenses);
        model.addAttribute("revenue", revenue);
        model.addAttribute("profit", profit);
        return "pl-report/index";
    }
} 