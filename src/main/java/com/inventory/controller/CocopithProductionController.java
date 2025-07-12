package com.inventory.controller;

import com.inventory.model.BlockProduction;
import com.inventory.model.CocopithProduction;
import com.inventory.model.FiberProduction;
import com.inventory.model.PithType;
import com.inventory.model.FiberType;
import com.inventory.repository.BlockProductionRepository;
import com.inventory.repository.CocopithProductionRepository;
import com.inventory.repository.FiberProductionRepository;
import com.inventory.service.StockService;
import com.inventory.service.FiberProductionService;
import com.inventory.service.LooseFiberStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.time.Duration;

@Controller
@RequestMapping("/cocopith-production")
@RequiredArgsConstructor
public class CocopithProductionController extends BaseController {

    private final StockService stockService;
    private final BlockProductionRepository blockProductionRepository;
    private final CocopithProductionRepository cocopithProductionRepository;
    private final FiberProductionRepository fiberProductionRepository;
    private final FiberProductionService fiberProductionService;
    private final LooseFiberStockService looseFiberStockService;

    @GetMapping
    public String showCocopithProductionPage(Model model) {
        try {
            model.addAttribute("currentPithStock", stockService.getCurrentPithStock());
            model.addAttribute("currentLowEcPithStock", stockService.getCurrentLowEcPithStock());
            model.addAttribute("normalBlockStock", stockService.getCurrentBlockStock(PithType.NORMAL));
            model.addAttribute("lowEcBlockStock", stockService.getCurrentBlockStock(PithType.LOW));
            
            // Add loose fiber stock for new fiber production tab
            model.addAttribute("whiteLooseFiberStock", looseFiberStockService.getCurrentStock(FiberType.WHITE));
            model.addAttribute("brownLooseFiberStock", looseFiberStockService.getCurrentStock(FiberType.BROWN));
            model.addAttribute("whiteFiberBaleStock", stockService.getCurrentFiberStock(FiberType.WHITE));
            model.addAttribute("brownFiberBaleStock", stockService.getCurrentFiberStock(FiberType.BROWN));
            
            // Add recent productions for display
            model.addAttribute("recentCocopithProductions", cocopithProductionRepository.findTop10ByOrderByProductionDateDesc());
            model.addAttribute("recentBlockProductions", blockProductionRepository.findTop10ByOrderByProductionTimeDesc());
            model.addAttribute("recentFiberProductions", fiberProductionRepository.findTop10ByOrderByProductionTimeDesc());
            
            model.addAttribute("activeTab", "cocopith-production");
            return "cocopith-production/index";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading cocopith production page: " + e.getMessage());
            return "error";
        }
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
            redirectAttributes.addFlashAttribute("success", 
                "Cocopith production completed successfully! " + pithQuantityUsed + 
                " kg High EC pith converted to Low EC pith by " + supervisorName + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cocopith-production";
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

            redirectAttributes.addFlashAttribute("success", 
                "Block production recorded successfully! " + blocksProduced + " blocks (" + 
                pithType.getDisplayName() + ") produced by " + supervisorName + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cocopith-production";
    }

    @PostMapping("/fiber")
    public String createFiberProduction(
            @RequestParam FiberType fiberType,
            @RequestParam Integer numberOfBales,
            @RequestParam String supervisorName,
            RedirectAttributes redirectAttributes) {
        try {
            fiberProductionService.convertLooseFiberToBales(fiberType, numberOfBales, supervisorName);
            
            double requiredFiber = fiberProductionService.calculateRequiredLooseFiber(numberOfBales);
            redirectAttributes.addFlashAttribute("success", 
                "Fiber production completed successfully! " + numberOfBales + " bales of " + 
                fiberType.getDisplayName() + " produced using " + requiredFiber + " kg loose fiber by " + supervisorName + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cocopith-production";
    }
}