package com.inventory.controller;

import com.inventory.model.Production;
import com.inventory.model.FiberType;
import com.inventory.model.HuskType;
import com.inventory.model.ShiftType;
import com.inventory.repository.BlockProductionRepository;
import com.inventory.service.ProductionService;
import com.inventory.service.StockService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/production")
public class ProductionController {

    private final ProductionService productionService;
    private final StockService stockService;
    private final BlockProductionRepository blockProductionRepository;
    private static final Logger log = LoggerFactory.getLogger(ProductionController.class);

    public ProductionController(ProductionService productionService,
            StockService stockService,
            BlockProductionRepository blockProductionRepository) {
        this.productionService = productionService;
        this.stockService = stockService;
        this.blockProductionRepository = blockProductionRepository;
    }

    @GetMapping
    public String showProductionPage(Model model) {
        try {
            // Get current stock for display
            double whiteFiberStock = stockService.getCurrentFiberStock(FiberType.WHITE);
            double brownFiberStock = stockService.getCurrentFiberStock(FiberType.BROWN);
            double pithStock = stockService.getCurrentPithStock();
            double lowEcPithStock = stockService.getCurrentLowEcPithStock();

            model.addAttribute("whiteFiberStock", whiteFiberStock);
            model.addAttribute("brownFiberStock", brownFiberStock);
            model.addAttribute("currentPithStock", pithStock);
            model.addAttribute("currentLowEcPithStock", lowEcPithStock);

            // Get recent productions for the table
            List<Production> recentProductions = productionService.getRecentProductions();
            model.addAttribute("recentProductions", recentProductions);

            // Set active tab for sidebar highlighting
            model.addAttribute("activeTab", "production");

            return "production/index";
        } catch (Exception e) {
            log.error("Error loading production page: {}", e.getMessage(), e);
            return "error";
        }
    }

    @PostMapping
    public String createProduction(
            @RequestParam("huskType") String huskType,
            @RequestParam("numBales") Integer numBales,
            @RequestParam("pithQuantity") Double pithQuantity,
            @RequestParam("batchCompletionTime") LocalDateTime batchCompletionTime,
            @RequestParam("supervisorName") String supervisorName,
            @RequestParam("shift") String shift,
            RedirectAttributes redirectAttributes) {

        try {
            Production production = new Production();
            production.setHuskType(HuskType.valueOf(huskType));
            production.setNumBales(numBales);
            production.setPithQuantity(pithQuantity);
            production.setBatchCompletionTime(batchCompletionTime);
            production.setSupervisorName(supervisorName);
            production.setShift(ShiftType.valueOf(shift));

            productionService.createProduction(production);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Production batch recorded successfully!");
        } catch (Exception e) {
            log.error("Error creating production: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error: " + e.getMessage());
        }

        return "redirect:/production";
    }

    @GetMapping("/report")
    public String showProductionReport(
            @RequestParam(value = "start", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "end", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            Model model) {

        // If dates not provided, default to current month
        LocalDate finalStartDate = startDate;
        LocalDate finalEndDate = endDate;

        if (finalStartDate == null) {
            finalStartDate = LocalDate.now().withDayOfMonth(1);
        }
        if (finalEndDate == null) {
            finalEndDate = LocalDate.now();
        }

        try {
            // Get productions for the date range
            List<Production> productions = productionService.getRecentProductions(); // Temporary fix

            // Use effectively final copies of the date variables
            final LocalDate filterStartDate = finalStartDate;
            final LocalDate filterEndDate = finalEndDate;

            // Filter productions by date range manually until proper method is available
            productions = productions.stream()
                    .filter(p -> {
                        LocalDate prodDate = p.getBatchCompletionTime().toLocalDate();
                        return !prodDate.isBefore(filterStartDate) && !prodDate.isAfter(filterEndDate);
                    })
                    .toList();

            // Calculate totals for summary
            int totalBatches = productions.size();
            int totalWhiteFiberBales = 0;
            int totalBrownFiberBales = 0;
            double totalPithProduced = 0;

            for (Production prod : productions) {
                if ("WHITE".equals(prod.getFiberType())) {
                    totalWhiteFiberBales += prod.getNumBales();
                } else if ("BROWN".equals(prod.getFiberType())) {
                    totalBrownFiberBales += prod.getNumBales();
                }
                totalPithProduced += prod.getPithQuantity();
            }

            model.addAttribute("productions", productions);
            model.addAttribute("startDate", finalStartDate);
            model.addAttribute("endDate", finalEndDate);
            model.addAttribute("totalBatches", totalBatches);
            model.addAttribute("totalWhiteFiberBales", totalWhiteFiberBales);
            model.addAttribute("totalBrownFiberBales", totalBrownFiberBales);
            model.addAttribute("totalPithProduced", totalPithProduced);
            model.addAttribute("activeTab", "production");

            return "production/report";
        } catch (Exception e) {
            log.error("Error generating production report: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error generating report: " + e.getMessage());
            return "error";
        }
    }
}