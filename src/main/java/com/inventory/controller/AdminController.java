package com.inventory.controller;

import com.inventory.model.Party;
import com.inventory.model.Dealer;
import com.inventory.service.PartyService;
import com.inventory.service.DealerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController extends BaseController {
    
    private final PartyService partyService;
    private final DealerService dealerService;
    
    @GetMapping
    public String showAdminPage(Model model) {
        model.addAttribute("party", new Party());
        model.addAttribute("dealer", new Dealer());
        model.addAttribute("parties", partyService.getAllParties());
        model.addAttribute("dealers", dealerService.getAllDealers());
        model.addAttribute("activeTab", "admin");
        return getViewPath("admin/index");
    }
    
    @PostMapping("/parties")
    public String saveParty(@ModelAttribute Party party, RedirectAttributes redirectAttributes) {
        try {
            partyService.saveParty(party);
            redirectAttributes.addFlashAttribute("success", "Party added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }
    
    @PostMapping("/dealers")
    public String saveDealer(@ModelAttribute Dealer dealer, RedirectAttributes redirectAttributes) {
        try {
            dealerService.saveDealer(dealer);
            redirectAttributes.addFlashAttribute("success", "Dealer added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }
    
    @DeleteMapping("/parties/{id}")
    public String deleteParty(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            partyService.deleteParty(id);
            redirectAttributes.addFlashAttribute("success", "Party deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }
    
    @DeleteMapping("/dealers/{id}")
    public String deleteDealer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            dealerService.deleteDealer(id);
            redirectAttributes.addFlashAttribute("success", "Dealer deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }
} 