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

@Controller
@RequestMapping("/production")
@RequiredArgsConstructor
public class ProductionController extends BaseController {

    private final ProductionService productionService;
    private final StockService stockService;
    private final BlockProductionRepository blockProductionRepository;

    @GetMapping
    public String index(Model model) {
        try {
            model.addAttribute("currentPithStock", stockService.getCurrentPithStock());
            model.addAttribute("currentLowEcPithStock", stockService.getCurrentLowEcPithStock());
            model.addAttribute("whiteFiberStock", stockService.getCurrentFiberStock(FiberType.WHITE));
            model.addAttribute("brownFiberStock", stockService.getCurrentFiberStock(FiberType.BROWN));
            model.addAttribute("blockStock", stockService.getCurrentBlockStock());
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
            @RequestParam String supervisorName,
            RedirectAttributes redirectAttributes) {
        try {
            stockService.convertToLowEcPith(pithQuantityUsed, supervisorName);
            redirectAttributes.addFlashAttribute("success", "Cocopith production completed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/production";
    }

    @GetMapping("/report")
    public String viewReport(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date, Model model) {
        model.addAttribute("date", date);
        model.addAttribute("firstShift", productionService.getProductionsByDateAndShift(date, ShiftType.FIRST));
        model.addAttribute("secondShift", productionService.getProductionsByDateAndShift(date, ShiftType.SECOND));
        return getViewPath("production-report/view");
    }

    @PostMapping("/block")
    public String createBlockProduction(
            @RequestParam PithType pithType,
            @RequestParam Integer blocksProduced,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime productionTime,
            RedirectAttributes redirectAttributes) {
        try {
            BlockProduction blockProduction = new BlockProduction();
            blockProduction.setPithType(pithType);
            blockProduction.setBlocksProduced(blocksProduced);
            blockProduction.setProductionTime(productionTime);

            stockService.processBlockProduction(blockProduction);
            blockProductionRepository.save(blockProduction);

            redirectAttributes.addFlashAttribute("success", "Block production recorded successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/production";
    }
}