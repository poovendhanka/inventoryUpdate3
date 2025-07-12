package com.inventory.controller;

import com.inventory.model.FiberProduction;
import com.inventory.model.FiberType;
import com.inventory.repository.FiberProductionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/fiber-report")
@RequiredArgsConstructor
public class FiberReportController extends BaseController {

    private final FiberProductionRepository fiberProductionRepository;

    @GetMapping
    public String showReportPage(Model model) {
        return "redirect:/reports?tab=fiber";
    }

    @GetMapping("/view")
    public String viewReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) FiberType fiberType,
            @RequestParam(required = false) Boolean downloadCsv,
            Model model,
            HttpServletResponse response) throws IOException {

        // Get fiber production data
        List<FiberProduction> fiberProductions;
        if (fiberType != null) {
            fiberProductions = fiberProductionRepository
                    .findByProductionDateBetweenAndFiberTypeOrderByProductionTimeDesc(startDate, endDate, fiberType);
        } else {
            fiberProductions = fiberProductionRepository
                    .findByProductionDateBetweenOrderByProductionTimeDesc(startDate, endDate);
        }

        // Calculate totals
        int totalBalesProduced = fiberProductions.stream()
                .mapToInt(FiberProduction::getBalesProduced)
                .sum();
        
        double totalLooseFiberConsumed = fiberProductions.stream()
                .mapToDouble(FiberProduction::getLooseFiberConsumed)
                .sum();

        // Calculate totals by fiber type
        int totalWhiteBales = fiberProductions.stream()
                .filter(fp -> fp.getFiberType() == FiberType.WHITE)
                .mapToInt(FiberProduction::getBalesProduced)
                .sum();

        int totalBrownBales = fiberProductions.stream()
                .filter(fp -> fp.getFiberType() == FiberType.BROWN)
                .mapToInt(FiberProduction::getBalesProduced)
                .sum();

        double totalWhiteFiberConsumed = fiberProductions.stream()
                .filter(fp -> fp.getFiberType() == FiberType.WHITE)
                .mapToDouble(FiberProduction::getLooseFiberConsumed)
                .sum();

        double totalBrownFiberConsumed = fiberProductions.stream()
                .filter(fp -> fp.getFiberType() == FiberType.BROWN)
                .mapToDouble(FiberProduction::getLooseFiberConsumed)
                .sum();

        // Handle CSV download if requested
        if (downloadCsv != null && downloadCsv) {
            generateCsvReport(fiberProductions, startDate, endDate, fiberType, response);
            return null;
        }

        // Add data to model
        model.addAttribute("fiberProductions", fiberProductions);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("fiberType", fiberType);
        model.addAttribute("totalBalesProduced", totalBalesProduced);
        model.addAttribute("totalLooseFiberConsumed", totalLooseFiberConsumed);
        model.addAttribute("totalWhiteBales", totalWhiteBales);
        model.addAttribute("totalBrownBales", totalBrownBales);
        model.addAttribute("totalWhiteFiberConsumed", totalWhiteFiberConsumed);
        model.addAttribute("totalBrownFiberConsumed", totalBrownFiberConsumed);

        // Set active tabs
        model.addAttribute("activeTab", "reports");
        model.addAttribute("activeReportTab", "fiber");

        return getViewPath("fiber-report/view");
    }

    private void generateCsvReport(List<FiberProduction> fiberProductions, LocalDate startDate, 
                                   LocalDate endDate, FiberType fiberType, HttpServletResponse response) 
                                   throws IOException {
        
        response.setContentType("text/csv");
        String filename = "fiber_production_report_" + startDate + "_to_" + endDate;
        if (fiberType != null) {
            filename += "_" + fiberType.name().toLowerCase();
        }
        filename += ".csv";
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        try (PrintWriter writer = response.getWriter()) {
            // Write CSV header
            writer.println("Date,Time,Fiber Type,Loose Fiber Consumed (kg),Bales Produced,Supervisor");

            // Write data rows
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            for (FiberProduction fp : fiberProductions) {
                writer.println(String.join(",",
                        fp.getProductionDate().format(dateFormatter),
                        fp.getProductionTime().format(timeFormatter),
                        fp.getFiberType().getDisplayName(),
                        String.valueOf(fp.getLooseFiberConsumed()),
                        String.valueOf(fp.getBalesProduced()),
                        fp.getSupervisorName() != null ? fp.getSupervisorName() : ""));
            }

            // Write totals
            int totalBales = fiberProductions.stream().mapToInt(FiberProduction::getBalesProduced).sum();
            double totalFiber = fiberProductions.stream().mapToDouble(FiberProduction::getLooseFiberConsumed).sum();
            
            writer.println();
            writer.println("TOTALS,,," + totalFiber + "," + totalBales + ",");
        }
    }
} 