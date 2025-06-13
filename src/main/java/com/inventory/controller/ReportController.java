package com.inventory.controller;

import com.inventory.service.DealerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController extends BaseController {

    private final DealerService dealerService;

    @GetMapping
    public String showReportsPage(
            @RequestParam(required = false) String tab,
            Model model,
            HttpServletRequest request) {
        model.addAttribute("activeTab", "reports");

        // Set default tab to production if not specified
        String activeTab = (tab != null) ? tab : "production";

        // Validate the tab value
        if (!activeTab.equals("production") && !activeTab.equals("cocopith") && !activeTab.equals("block") && !activeTab.equals("sales")) {
            activeTab = "production";
        }

        model.addAttribute("activeReportTab", activeTab);
        
        // Add dealers for sales report filtering
        model.addAttribute("dealers", dealerService.getAllDealers());
        
        return getViewPath("reports/index");
    }
}