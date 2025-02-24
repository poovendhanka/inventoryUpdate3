package com.inventory.controller;

import com.inventory.model.RawMaterial;
import com.inventory.service.RawMaterialService;
import com.inventory.service.PartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Controller
@RequestMapping("/raw-materials")
@RequiredArgsConstructor
public class RawMaterialController extends BaseController {
    private final RawMaterialService rawMaterialService;
    private final PartyService partyService;
    
    @GetMapping
    public String showRawMaterialsPage(Model model, HttpServletRequest request) {
        model.addAttribute("rawMaterial", new RawMaterial());
        model.addAttribute("parties", partyService.getAllParties());
        model.addAttribute("activeTab", "raw-materials");
        model.addAttribute("recentEntries", rawMaterialService.getRecentEntries());
        return getViewPath("raw-materials/index");
    }
    
    @PostMapping
    public String createRawMaterial(@ModelAttribute RawMaterial rawMaterial, RedirectAttributes redirectAttributes) {
        try {
            rawMaterialService.createRawMaterial(rawMaterial);
            redirectAttributes.addFlashAttribute("success", "Raw material added successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/raw-materials";
    }

    @GetMapping("/processed")
    public String showProcessedReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            Model model) {
        
        if (date == null) {
            date = LocalDate.now();
        }
        
        model.addAttribute("activeTab", "processed-raw-material");
        model.addAttribute("report", rawMaterialService.getProcessedReport(date));
        model.addAttribute("selectedDate", date);
        return getViewPath("raw-materials/processed");
    }
} 