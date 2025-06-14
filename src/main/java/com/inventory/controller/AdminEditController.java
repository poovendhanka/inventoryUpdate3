package com.inventory.controller;

import com.inventory.model.*;
import com.inventory.service.*;
import com.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/edit")
@RequiredArgsConstructor
public class AdminEditController extends BaseController {

    private final RawMaterialService rawMaterialService;
    private final ProductionService productionService;
    private final CocopithProductionRepository cocopithProductionRepository;
    private final BlockProductionRepository blockProductionRepository;
    private final SaleService saleService;
    private final ExpenseService expenseService;
    private final LabourEntryService labourEntryService;

    // Purchase Entries
    @GetMapping("/purchase")
    public String getPurchaseEntries(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                   Model model) {
        List<RawMaterial> entries;
        if (date != null) {
            entries = rawMaterialService.getRawMaterialsByDate(date);
        } else {
            entries = rawMaterialService.getRecentRawMaterials(20); // Get recent 20 entries
        }
        
        model.addAttribute("entries", entries);
        model.addAttribute("selectedDate", date);
        return "admin/fragments/purchase-entries";
    }

    @DeleteMapping("/purchase/{id}")
    public ResponseEntity<String> deletePurchaseEntry(@PathVariable Long id) {
        try {
            rawMaterialService.deleteRawMaterial(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("An unexpected error occurred while deleting the entry");
        }
    }

    // Production Entries
    @GetMapping("/production")
    public String getProductionEntries(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                     @RequestParam(required = false) String type,
                                     Model model) {
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedType", type);
        
        if ("production".equals(type)) {
            List<Production> entries;
            if (date != null) {
                entries = productionService.getProductionByDate(date);
            } else {
                entries = productionService.getRecentProduction(20);
            }
            model.addAttribute("productionEntries", entries);
        } else if ("cocopith".equals(type)) {
            List<CocopithProduction> entries;
            if (date != null) {
                LocalDateTime startDate = date.atStartOfDay();
                LocalDateTime endDate = date.plusDays(1).atStartOfDay();
                entries = cocopithProductionRepository.findByProductionDateBetweenOrderByProductionDateDesc(startDate, endDate);
            } else {
                entries = cocopithProductionRepository.findTopByOrderByProductionDateDesc(PageRequest.of(0, 20)).getContent();
            }
            model.addAttribute("cocopithEntries", entries);
        } else if ("block".equals(type)) {
            List<BlockProduction> entries;
            if (date != null) {
                LocalDateTime startDate = date.atStartOfDay();
                LocalDateTime endDate = date.plusDays(1).atStartOfDay();
                entries = blockProductionRepository.findByProductionTimeBetweenOrderByProductionTimeDesc(startDate, endDate);
            } else {
                entries = blockProductionRepository.findTopByOrderByProductionTimeDesc(PageRequest.of(0, 20)).getContent();
            }
            model.addAttribute("blockEntries", entries);
        } else {
            // Get all types
            if (date != null) {
                LocalDateTime startDate = date.atStartOfDay();
                LocalDateTime endDate = date.plusDays(1).atStartOfDay();
                model.addAttribute("productionEntries", productionService.getProductionByDate(date));
                model.addAttribute("cocopithEntries", cocopithProductionRepository.findByProductionDateBetweenOrderByProductionDateDesc(startDate, endDate));
                model.addAttribute("blockEntries", blockProductionRepository.findByProductionTimeBetweenOrderByProductionTimeDesc(startDate, endDate));
            } else {
                model.addAttribute("productionEntries", productionService.getRecentProduction(10));
                model.addAttribute("cocopithEntries", cocopithProductionRepository.findTopByOrderByProductionDateDesc(PageRequest.of(0, 10)).getContent());
                model.addAttribute("blockEntries", blockProductionRepository.findTopByOrderByProductionTimeDesc(PageRequest.of(0, 10)).getContent());
            }
        }
        
        return "admin/fragments/production-entries";
    }

    @DeleteMapping("/production/{type}/{id}")
    public ResponseEntity<Void> deleteProductionEntry(@PathVariable String type, @PathVariable Long id) {
        try {
            switch (type) {
                case "production":
                    productionService.deleteProduction(id);
                    break;
                case "cocopith":
                    cocopithProductionRepository.deleteById(id);
                    break;
                case "block":
                    blockProductionRepository.deleteById(id);
                    break;
                default:
                    return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Sales Entries
    @GetMapping("/sales")
    public String getSalesEntries(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                @RequestParam(required = false) String productType,
                                Model model) {
        List<Sale> entries;
        if (date != null) {
            entries = saleService.getSalesByDate(date);
            if (productType != null && !productType.isEmpty()) {
                entries = entries.stream()
                    .filter(sale -> sale.getProductType().name().equals(productType))
                    .toList();
            }
        } else {
            entries = saleService.getRecentSales();
            if (productType != null && !productType.isEmpty()) {
                entries = entries.stream()
                    .filter(sale -> sale.getProductType().name().equals(productType))
                    .toList();
            }
        }
        
        model.addAttribute("entries", entries);
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedProductType", productType);
        return "admin/fragments/sales-entries";
    }

    @DeleteMapping("/sales/{id}")
    public ResponseEntity<Void> deleteSalesEntry(@PathVariable Long id) {
        try {
            saleService.deleteSale(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Expense Entries
    @GetMapping("/expenses")
    public String getExpenseEntries(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                  @RequestParam(required = false) String type,
                                  Model model) {
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedType", type);
        
        if ("labour".equals(type)) {
            List<LabourEntry> entries;
            if (date != null) {
                entries = labourEntryService.getLabourEntriesByDate(date);
            } else {
                entries = labourEntryService.getRecentLabourEntries(20);
            }
            model.addAttribute("labourEntries", entries);
        } else if ("general".equals(type)) {
            List<Expense> entries;
            if (date != null) {
                entries = expenseService.getExpensesByDate(date);
            } else {
                entries = expenseService.getRecentExpenses(20);
            }
            model.addAttribute("generalEntries", entries);
        } else {
            // Get all types
            if (date != null) {
                model.addAttribute("labourEntries", labourEntryService.getLabourEntriesByDate(date));
                model.addAttribute("generalEntries", expenseService.getExpensesByDate(date));
            } else {
                model.addAttribute("labourEntries", labourEntryService.getRecentLabourEntries(10));
                model.addAttribute("generalEntries", expenseService.getRecentExpenses(10));
            }
        }
        
        return "admin/fragments/expense-entries";
    }

    @DeleteMapping("/expenses/{type}/{id}")
    public ResponseEntity<Void> deleteExpenseEntry(@PathVariable String type, @PathVariable Long id) {
        try {
            if ("labour".equals(type)) {
                labourEntryService.deleteLabourEntry(id);
            } else if ("general".equals(type)) {
                expenseService.deleteExpense(id);
            } else {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 