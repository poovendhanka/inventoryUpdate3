package com.inventory.controller;

import com.inventory.model.Party;
import com.inventory.service.PartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/parties")
@RequiredArgsConstructor
public class PartyController extends BaseController {
    private final PartyService partyService;
    
    @GetMapping("/{id}")
    @ResponseBody
    public Party getParty(@PathVariable Long id) {
        return partyService.getPartyById(id);
    }
    
    @GetMapping
    public String showPartiesPage(Model model) {
        model.addAttribute("parties", partyService.getAllParties());
        model.addAttribute("party", new Party());
        return "admin/parties";
    }
    
    @PostMapping
    public String saveParty(@ModelAttribute Party party) {
        partyService.saveParty(party);
        return "redirect:/admin/parties";
    }
    
    @DeleteMapping("/{id}")
    public String deleteParty(@PathVariable Long id) {
        partyService.deleteParty(id);
        return "redirect:/admin/parties";
    }
} 