package com.inventory.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController extends BaseController {
    
    @GetMapping
    public String showReportsPage(Model model, HttpServletRequest request) {
        model.addAttribute("activeTab", "reports");
        return getViewPath("reports/index");
    }
} 