package com.inventory.controller;

import com.inventory.model.*;
import com.inventory.service.AccountService;
import com.inventory.service.RawMaterialService;
import com.inventory.service.BillPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController extends BaseController {
    private final AccountService accountService;
    private final RawMaterialService rawMaterialService;
    private final BillPdfService billPdfService;
    
    @GetMapping
    public String showAccountsPage(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate processedDate,
                                  Model model, HttpServletRequest request) {
        model.addAttribute("activeTab", "accounts");
        model.addAttribute("unprocessedEntries", rawMaterialService.getUnprocessedEntries());
        
        if (processedDate != null) {
            // Show processed entries for specific date
            model.addAttribute("processedEntriesForDate", rawMaterialService.getProcessedEntriesByDate(processedDate));
            model.addAttribute("selectedProcessedDate", processedDate);
        } else {
            // Show recent processed entries (default behavior)
            model.addAttribute("recentProcessedEntries", rawMaterialService.getRecentProcessedEntries());
        }
        
        return getViewPath("accounts/index");
    }
    
    @PostMapping("/process")
    public String processRawMaterial(@RequestParam Long rawMaterialId,
                                   @RequestParam Double costPerCft,
                                   @RequestParam String supervisorName,
                                   Model model) {
        try {
            rawMaterialService.processRawMaterial(rawMaterialId, costPerCft, supervisorName);
            model.addAttribute("success", "Raw material processed successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Error processing raw material: " + e.getMessage());
        }
        return "redirect:/accounts";
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

    @GetMapping("/bill/{processedId}")
    public ResponseEntity<byte[]> generateBill(@PathVariable Long processedId) {
        try {
            ProcessedRawMaterial processed = rawMaterialService.getProcessedById(processedId);
            if (processed == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] pdfBytes = billPdfService.generateRawMaterialBill(processed);
            
            String filename = "raw_material_bill_" + processed.getRawMaterial().getReceiptNumber() + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            log.error("Error generating bill for processed ID {}: {}", processedId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 