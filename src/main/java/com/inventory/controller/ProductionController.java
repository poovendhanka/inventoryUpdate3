package com.inventory.controller;

import com.inventory.model.Production;
import com.inventory.model.ShiftType;
import com.inventory.service.ProductionService;
import com.inventory.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.inventory.model.FiberType;
import com.inventory.model.BlockProduction;
import com.inventory.repository.BlockProductionRepository;
import com.inventory.model.PithType;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

@Controller
@RequestMapping("/production")
@RequiredArgsConstructor
public class ProductionController extends BaseController {

    private final ProductionService productionService;
    private final StockService stockService;
    private final BlockProductionRepository blockProductionRepository;

    @GetMapping
    public String showProductionPage(Model model) {
        try {
            model.addAttribute("currentPithStock", stockService.getCurrentPithStock());
            model.addAttribute("currentLowEcPithStock", stockService.getCurrentLowEcPithStock());
            model.addAttribute("whiteFiberStock", stockService.getCurrentFiberStock(FiberType.WHITE));
            model.addAttribute("brownFiberStock", stockService.getCurrentFiberStock(FiberType.BROWN));
            model.addAttribute("normalBlockStock", stockService.getCurrentBlockStock(PithType.NORMAL));
            model.addAttribute("lowEcBlockStock", stockService.getCurrentBlockStock(PithType.LOW));
            model.addAttribute("recentProductions", productionService.getRecentProductions());
            return "production/index";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading production page: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping
    public String createProduction(@ModelAttribute Production production, RedirectAttributes redirectAttributes) {
        try {
            // Set system time
            production.setSystemTime(LocalDateTime.now());

            // Validate required fields
            if (production.getHuskType() == null) {
                throw new RuntimeException("Husk type must be specified");
            }
            if (production.getShift() == null) {
                throw new RuntimeException("Shift must be specified");
            }
            if (production.getSupervisorName() == null || production.getSupervisorName().trim().isEmpty()) {
                throw new RuntimeException("Supervisor name must be specified");
            }
            if (production.getBatchCompletionTime() == null) {
                throw new RuntimeException("Batch completion time must be specified");
            }

            // Create production
            productionService.createProduction(production);
            redirectAttributes.addFlashAttribute("success", "Production batch completed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/production";
        }
        return "redirect:/production";
    }

    @PostMapping("/cocopith")
    public String createCocopithProduction(
            @RequestParam Double pithQuantityUsed,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime productionTime,
            @RequestParam String supervisorName,
            RedirectAttributes redirectAttributes) {
        try {
            LocalDateTime systemTime = LocalDateTime.now();
            Duration duration = Duration.between(productionTime, systemTime);
            stockService.convertToLowEcPith(pithQuantityUsed, supervisorName, duration, productionTime);
            redirectAttributes.addFlashAttribute("success", "Cocopith production completed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/production";
    }

    @GetMapping("/report")
    public String viewReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            Model model) {
        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

            // Get productions by shift
            List<Production> firstShift = productionService.getProductionsByDateAndShift(date, ShiftType.FIRST);
            List<Production> secondShift = productionService.getProductionsByDateAndShift(date, ShiftType.SECOND);

            // Calculate durations
            calculateDurations(firstShift);
            calculateDurations(secondShift);

            // Add specific production type counts
            addProductionSummaries(model, firstShift, secondShift);

            model.addAttribute("date", date);
            model.addAttribute("firstShift", firstShift);
            model.addAttribute("secondShift", secondShift);

            return "production/report";
        } catch (Exception e) {
            model.addAttribute("error", "Error generating report: " + e.getMessage());
            return "error";
        }
    }

    private void addProductionSummaries(Model model, List<Production> firstShift, List<Production> secondShift) {
        // Add Low EC Pith summaries
        int normalPithUsed = calculateTotalPithUsed(firstShift, secondShift);
        int lowEcPithProduced = calculateTotalLowEcPithProduced(firstShift, secondShift);

        // Add Block Production summaries
        int normalBlocksProduced = calculateTotalBlocksProduced(firstShift, secondShift, PithType.NORMAL);
        int lowEcBlocksProduced = calculateTotalBlocksProduced(firstShift, secondShift, PithType.LOW);

        model.addAttribute("normalPithUsed", normalPithUsed);
        model.addAttribute("lowEcPithProduced", lowEcPithProduced);
        model.addAttribute("normalBlocksProduced", normalBlocksProduced);
        model.addAttribute("lowEcBlocksProduced", lowEcBlocksProduced);
    }

    @PostMapping("/block")
    public String createBlockProduction(
            @RequestParam PithType pithType,
            @RequestParam Integer blocksProduced,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime productionTime,
            @RequestParam String supervisorName,
            RedirectAttributes redirectAttributes) {
        try {
            BlockProduction blockProduction = new BlockProduction();
            blockProduction.setPithType(pithType);
            blockProduction.setBlocksProduced(blocksProduced);
            blockProduction.setProductionTime(productionTime);
            blockProduction.setSupervisorName(supervisorName);

            stockService.processBlockProduction(blockProduction);
            blockProductionRepository.save(blockProduction);

            redirectAttributes.addFlashAttribute("success", "Block production recorded successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/production";
    }

    private void calculateDurations(List<Production> productions) {
        productions.forEach(production -> {
            if (production.getBatchCompletionTime() != null && production.getSystemTime() != null) {
                Duration duration = Duration.between(production.getSystemTime(),
                        production.getBatchCompletionTime());
                production.setDuration(duration);
            }
        });
    }

    private int calculateTotalPithUsed(List<Production> firstShift, List<Production> secondShift) {
        return productionService.calculateTotalPithUsed(firstShift, secondShift);
    }

    private int calculateTotalLowEcPithProduced(List<Production> firstShift, List<Production> secondShift) {
        return productionService.calculateTotalLowEcPithProduced(firstShift, secondShift);
    }

    private int calculateTotalBlocksProduced(List<Production> firstShift, List<Production> secondShift,
            PithType pithType) {
        return productionService.calculateTotalBlocksProduced(firstShift, secondShift, pithType);
    }
}