package com.inventory.controller;

import com.inventory.model.ProcessedRawMaterial;
import com.inventory.service.RawMaterialService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/purchase-report")
@RequiredArgsConstructor
public class PurchaseReportController extends BaseController {

    private final RawMaterialService rawMaterialService;

    @GetMapping
    public String showReportPage(Model model) {
        return "redirect:/reports?tab=purchase";
    }

    @GetMapping("/view")
    public String viewReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
            @RequestParam(required = false) Boolean downloadCsv,
            Model model,
            HttpServletResponse response) {

        // Convert dates to LocalDateTime for the repository query
        LocalDateTime startDateTime = fromDate.atStartOfDay();
        LocalDateTime endDateTime = toDate.atTime(23, 59, 59);

        // Get purchase data (processed raw materials)
        List<ProcessedRawMaterial> purchases = rawMaterialService.getProcessedEntriesByDateRange(startDateTime, endDateTime);

        // Calculate totals
        double totalCft = purchases.stream()
                .mapToDouble(p -> p.getRawMaterial().getCft() != null ? p.getRawMaterial().getCft() : 0.0)
                .sum();
        
        double totalCost = purchases.stream()
                .mapToDouble(p -> p.getTotalCost() != null ? p.getTotalCost() : 0.0)
                .sum();

        // Handle CSV download if requested
        if (downloadCsv != null && downloadCsv) {
            try {
                generateCsvReport(purchases, fromDate, toDate, response);
                return null; // Don't render the view
            } catch (IOException e) {
                // Handle exception
                e.printStackTrace();
                model.addAttribute("error", "Error generating CSV report");
            }
        }

        // Add data to model
        model.addAttribute("purchases", purchases);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("totalCft", totalCft);
        model.addAttribute("totalCost", totalCost);
        model.addAttribute("activeTab", "reports");
        model.addAttribute("activeReportTab", "purchase");

        return getViewPath("purchase-report/view");
    }

    private void generateCsvReport(List<ProcessedRawMaterial> purchases, LocalDate fromDate, LocalDate toDate, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"purchase_report_" + fromDate + "_to_" + toDate + ".csv\"");
        
        PrintWriter writer = response.getWriter();
        
        // Write CSV header
        writer.println("Receipt Number,Lorry In Time,Vehicle Number,Party Name,Husk Type,CFT,Cost Per CFT,Total Cost,Accounts Supervisor,Processed Date");
        
        // Write data rows
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        for (ProcessedRawMaterial purchase : purchases) {
            writer.printf("%s,%s,%s,%s,%s,%.2f,%.2f,%.2f,%s,%s%n",
                    purchase.getRawMaterial().getReceiptNumber(),
                    purchase.getRawMaterial().getLorryInTime().format(formatter),
                    purchase.getRawMaterial().getVehicleNumber(),
                    purchase.getRawMaterial().getParty().getName(),
                    purchase.getRawMaterial().getHuskType().getDisplayName(),
                    purchase.getRawMaterial().getCft(),
                    purchase.getCostPerCft() != null ? purchase.getCostPerCft() : 0.0,
                    purchase.getTotalCost() != null ? purchase.getTotalCost() : 0.0,
                    purchase.getAccountsSupervisor(),
                    purchase.getProcessedDate().format(formatter)
            );
        }
        
        writer.flush();
    }
} 