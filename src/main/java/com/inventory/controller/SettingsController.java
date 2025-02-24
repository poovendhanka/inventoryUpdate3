package com.inventory.controller;

import com.inventory.model.Party;
import com.inventory.model.Dealer;
import com.inventory.service.PartyService;
import com.inventory.service.DealerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController extends BaseController {
    
    private final PartyService partyService;
    private final DealerService dealerService;
    
    @GetMapping
    public String showSettingsPage(Model model, HttpServletRequest request) {
        model.addAttribute("activeTab", "settings");
        model.addAttribute("party", new Party());
        model.addAttribute("dealer", new Dealer());
        return getViewPath("settings/index");
    }
    
    @PostMapping("/party")
    public String addParty(@ModelAttribute Party party) {
        partyService.saveParty(party);
        return "redirect:/settings";
    }
    
    @PostMapping("/dealer")
    public String addDealer(@ModelAttribute Dealer dealer) {
        dealerService.saveDealer(dealer);
        return "redirect:/settings";
    }
} 