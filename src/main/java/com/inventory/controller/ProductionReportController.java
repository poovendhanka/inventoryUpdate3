package com.inventory.controller;

import com.inventory.model.Production;
import com.inventory.model.ShiftType;
import com.inventory.service.ProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.PrintWriter;
import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/production-report")
@RequiredArgsConstructor
public class ProductionReportController extends BaseController {

    private final ProductionService productionService;

    @GetMapping
    public String showReportPage(Model model, HttpServletRequest request) {
        return "redirect:/reports";
    }

    @GetMapping("/view")
    public String viewReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
            @RequestParam(required = false, defaultValue = "false") boolean downloadCsv,
            Model model,
            HttpServletResponse response) throws IOException {

        // For backward compatibility, if both dates are the same, treat as a single day
        // report
        boolean isSingleDayReport = fromDate.isEqual(toDate);
        LocalDate reportDate = isSingleDayReport ? fromDate : null;

        List<Production> allProductions = null;
        List<Production> firstShift = null;
        List<Production> secondShift = null;
        int totalBales = 0;
        int totalBoxes = 0;

        // If single day report, use existing logic
        if (isSingleDayReport) {
            // Get productions for both shifts for the single date
            firstShift = productionService.getProductionsByDateAndShift(fromDate, ShiftType.FIRST);
            secondShift = productionService.getProductionsByDateAndShift(fromDate, ShiftType.SECOND);

            // Calculate durations for first shift
            calculateDurations(firstShift, fromDate, ShiftType.FIRST);

            // Calculate durations for second shift
            calculateDurations(secondShift, fromDate, ShiftType.SECOND);

            // Calculate totals
            totalBales = calculateTotalBales(firstShift, secondShift);
            totalBoxes = calculateTotalBoxes(firstShift, secondShift);

            // Calculate shift-specific totals
            int firstShiftBales = calculateShiftBales(firstShift);
            int firstShiftBoxes = calculateShiftBoxes(firstShift);
            int secondShiftBales = calculateShiftBales(secondShift);
            int secondShiftBoxes = calculateShiftBoxes(secondShift);

            // Add data to model
            model.addAttribute("date", fromDate);
            model.addAttribute("firstShift", firstShift);
            model.addAttribute("secondShift", secondShift);
            model.addAttribute("totalBales", totalBales);
            model.addAttribute("totalBoxes", totalBoxes);
            model.addAttribute("firstShiftBales", firstShiftBales);
            model.addAttribute("firstShiftBoxes", firstShiftBoxes);
            model.addAttribute("secondShiftBales", secondShiftBales);
            model.addAttribute("secondShiftBoxes", secondShiftBoxes);

            // Combine first and second shift for CSV export if needed
            allProductions = new java.util.ArrayList<>();
            allProductions.addAll(firstShift);
            allProductions.addAll(secondShift);
        } else {
            // Date range report logic
            // Get all productions within the date range
            allProductions = productionService.getProductionsByDateRange(fromDate, toDate);

            // Sort all productions by date and time
            allProductions.sort((p1, p2) -> p1.getBatchCompletionTime().compareTo(p2.getBatchCompletionTime()));

            // Calculate durations for productions in date range
            Map<LocalDate, List<Production>> productionsByDate = allProductions.stream()
                    .collect(Collectors.groupingBy(p -> p.getBatchCompletionTime().toLocalDate()));

            // Calculate durations for each date and shift
            productionsByDate.forEach((date, prods) -> {
                Map<ShiftType, List<Production>> productionsByShift = prods.stream()
                        .collect(Collectors.groupingBy(Production::getShift));

                productionsByShift.forEach((shift, shiftProductions) -> {
                    // Sort by batch completion time before calculating durations
                    shiftProductions
                            .sort((p1, p2) -> p1.getBatchCompletionTime().compareTo(p2.getBatchCompletionTime()));
                    calculateDurations(shiftProductions, date, shift);
                });
            });

            // Calculate summary totals for the date range
            totalBales = allProductions.stream().mapToInt(p -> p.getNumBales() != null ? p.getNumBales() : 0).sum();
            totalBoxes = allProductions.stream().mapToInt(p -> p.getNumBoxes() != null ? p.getNumBoxes() : 0).sum();

            // Add data to model for date range report
            model.addAttribute("fromDate", fromDate);
            model.addAttribute("toDate", toDate);
            model.addAttribute("allProductions", allProductions);
            model.addAttribute("productionsByDate", productionsByDate);
            model.addAttribute("totalBales", totalBales);
            model.addAttribute("totalBoxes", totalBoxes);
        }

        model.addAttribute("activeTab", "reports");
        model.addAttribute("activeReportTab", "production");

        // Handle CSV download if requested
        if (downloadCsv) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition",
                    "attachment; filename=production_report_" + fromDate + "_to_" + toDate + ".csv");

            try (PrintWriter writer = response.getWriter()) {
                // Write CSV header
                writer.println("Date & Time,Shift,Fiber Type,Duration,Bales,Pith (kg)");

                // Write data rows
                for (Production prod : allProductions) {
                    String fiberType = prod.getFiberType() != null ? prod.getFiberType().toString() : "";
                    int bales = prod.getNumBales() != null ? prod.getNumBales() : 0;
                    double pith = prod.getPithQuantity() != null ? prod.getPithQuantity() : 0;
                    String duration = prod.getTimeTaken() != null
                            ? prod.getTimeTaken().toHours() + "h " + prod.getTimeTaken().toMinutesPart() + "m"
                            : "";
                    String dateTime = prod.getBatchCompletionTime().toLocalDate().toString() + " "
                            + prod.getBatchCompletionTime().toLocalTime().toString().substring(0, 5);

                    writer.println(String.join(",",
                            dateTime,
                            prod.getShift().toString(),
                            fiberType,
                            duration,
                            String.valueOf(bales),
                            pith + " kg"));
                }

                // Write total row
                writer.println("TOTAL,,,,," + totalBales + ",");
            }
            return null;
        }

        return getViewPath("production-report/view");
    }

    private void calculateDurations(List<Production> productions, LocalDate date, ShiftType shift) {
        if (productions.isEmpty()) {
            return;
        }

        // Set shift start time
        LocalDateTime shiftStartTime;
        if (shift == ShiftType.FIRST) {
            // First shift starts at 8 AM
            shiftStartTime = date.atTime(8, 0);
        } else {
            // Second shift starts at 8 PM
            shiftStartTime = date.atTime(20, 0);
        }

        // Calculate duration for first batch
        Production firstBatch = productions.get(0);
        LocalDateTime batchTime = firstBatch.getBatchCompletionTime();

        // Handle case where second shift completion time is after midnight
        if (shift == ShiftType.SECOND && batchTime.toLocalDate().isAfter(date)) {
            // Time is already correct, no adjustment needed
        } else if (shift == ShiftType.SECOND && batchTime.getHour() < 8) {
            // If completion time is after midnight but before 8 AM, it's still part of the
            // second shift
            // No adjustment needed
        } else if (batchTime.isBefore(shiftStartTime)) {
            // If batch time is before shift start (shouldn't happen normally), use shift
            // start
            batchTime = shiftStartTime;
        }

        firstBatch.setTimeTaken(Duration.between(shiftStartTime, batchTime));

        // Calculate durations for subsequent batches
        for (int i = 1; i < productions.size(); i++) {
            Production currentBatch = productions.get(i);
            Production previousBatch = productions.get(i - 1);

            currentBatch.setTimeTaken(Duration.between(
                    previousBatch.getBatchCompletionTime(),
                    currentBatch.getBatchCompletionTime()));
        }
    }

    private int calculateTotalBales(List<Production> firstShift, List<Production> secondShift) {
        int firstShiftBales = calculateShiftBales(firstShift);
        int secondShiftBales = calculateShiftBales(secondShift);
        return firstShiftBales + secondShiftBales;
    }

    private int calculateTotalBoxes(List<Production> firstShift, List<Production> secondShift) {
        int firstShiftBoxes = calculateShiftBoxes(firstShift);
        int secondShiftBoxes = calculateShiftBoxes(secondShift);
        return firstShiftBoxes + secondShiftBoxes;
    }

    private int calculateShiftBales(List<Production> shift) {
        return shift.stream()
                .mapToInt(p -> p.getNumBales() != null ? p.getNumBales() : 0)
                .sum();
    }

    private int calculateShiftBoxes(List<Production> shift) {
        return shift.stream()
                .mapToInt(p -> p.getNumBoxes() != null ? p.getNumBoxes() : 0)
                .sum();
    }
}