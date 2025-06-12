package com.inventory.controller;

import com.inventory.model.BlockProduction;
import com.inventory.model.PithType;
import com.inventory.repository.BlockProductionRepository;
import com.inventory.service.StockService;
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

    @GetMapping
    public String showCocopithProductionPage(Model model) {
        try {
            model.addAttribute("currentPithStock", stockService.getCurrentPithStock());
            model.addAttribute("currentLowEcPithStock", stockService.getCurrentLowEcPithStock());
            model.addAttribute("normalBlockStock", stockService.getCurrentBlockStock(PithType.NORMAL));
            model.addAttribute("lowEcBlockStock", stockService.getCurrentBlockStock(PithType.LOW));
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
}