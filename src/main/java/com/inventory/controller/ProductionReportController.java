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
    public String viewReport(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date, Model model) {
        // Get productions for both shifts
        List<Production> firstShift = productionService.getProductionsByDateAndShift(date, ShiftType.FIRST);
        List<Production> secondShift = productionService.getProductionsByDateAndShift(date, ShiftType.SECOND);

        // Calculate durations for first shift
        calculateDurations(firstShift, date, ShiftType.FIRST);

        // Calculate durations for second shift
        calculateDurations(secondShift, date, ShiftType.SECOND);

        // Calculate totals
        int totalBales = calculateTotalBales(firstShift, secondShift);
        int totalBoxes = calculateTotalBoxes(firstShift, secondShift);

        // Calculate shift-specific totals
        int firstShiftBales = calculateShiftBales(firstShift);
        int firstShiftBoxes = calculateShiftBoxes(firstShift);
        int secondShiftBales = calculateShiftBales(secondShift);
        int secondShiftBoxes = calculateShiftBoxes(secondShift);

        // Add data to model
        model.addAttribute("date", date);
        model.addAttribute("firstShift", firstShift);
        model.addAttribute("secondShift", secondShift);
        model.addAttribute("totalBales", totalBales);
        model.addAttribute("totalBoxes", totalBoxes);
        model.addAttribute("firstShiftBales", firstShiftBales);
        model.addAttribute("firstShiftBoxes", firstShiftBoxes);
        model.addAttribute("secondShiftBales", secondShiftBales);
        model.addAttribute("secondShiftBoxes", secondShiftBoxes);
        model.addAttribute("activeTab", "reports");
        model.addAttribute("activeReportTab", "production");

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