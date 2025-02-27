package com.inventory.controller;

import com.inventory.model.CocopithProduction;
import com.inventory.repository.CocopithProductionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/cocopith-report")
@RequiredArgsConstructor
public class CocopithProductionReportController extends BaseController {

        private final CocopithProductionRepository cocopithProductionRepository;

        @GetMapping
        public String showReportPage(Model model) {
                model.addAttribute("activeTab", "cocopith-report");
                return getViewPath("cocopith-report/index");
        }

        @GetMapping("/view")
        public String viewReport(
                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                        Model model) {

                // Convert dates to LocalDateTime for the repository query
                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

                // Get cocopith production data
                List<CocopithProduction> productions = cocopithProductionRepository
                                .findByProductionDateBetweenOrderByProductionDateDesc(startDateTime, endDateTime);

                // Calculate totals
                double totalPithUsed = productions.stream()
                                .mapToDouble(CocopithProduction::getPithQuantityUsed)
                                .sum();

                double totalLowEcProduced = productions.stream()
                                .mapToDouble(CocopithProduction::getLowEcQuantityProduced)
                                .sum();

                // Add data to model
                model.addAttribute("productions", productions);
                model.addAttribute("startDate", startDate);
                model.addAttribute("endDate", endDate);
                model.addAttribute("totalPithUsed", totalPithUsed);
                model.addAttribute("totalLowEcProduced", totalLowEcProduced);
                model.addAttribute("activeTab", "cocopith-report");

                return getViewPath("cocopith-report/view");
        }
}