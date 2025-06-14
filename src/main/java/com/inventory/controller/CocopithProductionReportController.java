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

import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/cocopith-report")
@RequiredArgsConstructor
public class CocopithProductionReportController extends BaseController {

        private final CocopithProductionRepository cocopithProductionRepository;

        @GetMapping
        public String showReportPage(Model model) {
                return "redirect:/reports?tab=cocopith";
        }

        @GetMapping("/view")
        public String viewReport(
                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                        @RequestParam(required = false) Boolean downloadCsv,
                        Model model,
                        HttpServletResponse response) {

                // Convert dates to LocalDateTime for the repository query
                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

                // Get cocopith production data
                List<CocopithProduction> productions = cocopithProductionRepository
                                .findByProductionDateBetweenOrderByProductionDateDesc(startDateTime, endDateTime);

                // Calculate total Low EC pith produced
                double totalLowEcProduced = productions.stream()
                                .mapToDouble(CocopithProduction::getLowEcQuantityProduced)
                                .sum();

                // Handle CSV download if requested
                if (downloadCsv != null && downloadCsv) {
                        response.setContentType("text/csv");
                        response.setHeader("Content-Disposition",
                                        "attachment; filename=cocopith_production_report_" + startDate + "_to_" + endDate + ".csv");

                        try (PrintWriter writer = response.getWriter()) {
                                // Write CSV header
                                writer.println("Date & Time,Production Start Time,System Time,Production Time,Low EC Pith Produced (kg),Supervisor");

                                // Write data rows
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                                for (CocopithProduction production : productions) {
                                        String dateTime = production.getProductionDate() != null 
                                                ? production.getProductionDate().format(formatter) : "";
                                        String startTime = production.getProductionStartTime() != null 
                                                ? production.getProductionStartTime().format(formatter) : "";
                                        String systemTime = production.getSystemTime() != null 
                                                ? production.getSystemTime().format(formatter) : "";
                                        String productionTime = production.getProductionTime() != null 
                                                ? production.getProductionTime().toHours() + "h " + production.getProductionTime().toMinutesPart() + "m" : "";
                                        String lowEcQuantity = String.valueOf(production.getLowEcQuantityProduced());
                                        String supervisor = production.getSupervisorName() != null ? production.getSupervisorName() : "";

                                        writer.println(String.join(",",
                                                "\"" + dateTime + "\"",
                                                "\"" + startTime + "\"",
                                                "\"" + systemTime + "\"",
                                                "\"" + productionTime + "\"",
                                                lowEcQuantity,
                                                "\"" + supervisor + "\""));
                                }

                                // Write total row
                                writer.println("TOTAL,,,," + totalLowEcProduced + ",");
                        } catch (Exception e) {
                                // Handle exception
                                e.printStackTrace();
                        }
                        return null;
                }

                // Add data to model
                model.addAttribute("productions", productions);
                model.addAttribute("startDate", startDate);
                model.addAttribute("endDate", endDate);
                model.addAttribute("totalLowEcProduced", totalLowEcProduced);
                model.addAttribute("activeTab", "reports");
                model.addAttribute("activeReportTab", "cocopith");

                return getViewPath("cocopith-report/view");
        }
}