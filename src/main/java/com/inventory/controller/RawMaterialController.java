package com.inventory.controller;

import com.inventory.model.RawMaterial;
import com.inventory.service.RawMaterialService;
import com.inventory.service.PartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.io.PrintWriter;
import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import com.inventory.dto.ProcessedRawMaterialReport;
import com.inventory.model.ProcessedRawMaterial;

@Controller
@RequestMapping("/raw-materials")
@RequiredArgsConstructor
public class RawMaterialController extends BaseController {
    private final RawMaterialService rawMaterialService;
    private final PartyService partyService;

    @GetMapping
    public String showRawMaterialsPage(Model model, HttpServletRequest request) {
        model.addAttribute("rawMaterial", new RawMaterial());
        model.addAttribute("parties", partyService.getAllParties());
        model.addAttribute("activeTab", "raw-materials");
        model.addAttribute("recentEntries", rawMaterialService.getRecentEntries());
        return getViewPath("raw-materials/index");
    }

    @PostMapping
    public String createRawMaterial(@ModelAttribute RawMaterial rawMaterial, RedirectAttributes redirectAttributes) {
        try {
            rawMaterialService.createRawMaterial(rawMaterial);
            redirectAttributes.addFlashAttribute("success", "Raw material added successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/raw-materials";
    }

    @GetMapping("/processed")
    public String showProcessedReport(Model model) {
        model.addAttribute("activeTab", "processed-raw-material");
        return getViewPath("raw-material/index");
    }

    @GetMapping("/processed/view")
    public String viewProcessedReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            Model model) {

        model.addAttribute("activeTab", "processed-raw-material");
        model.addAttribute("report", rawMaterialService.getProcessedReport(startDate, endDate));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return getViewPath("raw-material/view");
    }

    @GetMapping("/processed/export")
    public void exportProcessedReportCsv(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            HttpServletResponse response) throws IOException {

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=processed_raw_materials_"
                + startDate + "_to_" + endDate + ".csv");

        ProcessedRawMaterialReport report = rawMaterialService.getProcessedReport(startDate, endDate);

        try (PrintWriter writer = response.getWriter()) {
            // Write CSV header
            writer.println(
                    "Receipt Number,Date,Vehicle Number,Party,Supervisor,Cost,Processed Date,Accounts Supervisor");

            // Write data rows
            for (ProcessedRawMaterial entry : report.getProcessedEntries()) {
                RawMaterial rawMaterial = entry.getRawMaterial();
                writer.println(String.join(",",
                        rawMaterial.getReceiptNumber(),
                        rawMaterial.getLorryInTime().toLocalDate().toString(),
                        rawMaterial.getVehicleNumber(),
                        rawMaterial.getParty() != null ? "\"" + rawMaterial.getParty().getName() + "\"" : "",
                        "\"" + rawMaterial.getSupervisorName() + "\"",
                        entry.getCost().toString(),
                        entry.getProcessedDate().toLocalDate().toString(),
                        "\"" + entry.getAccountsSupervisor() + "\""));
            }
        }
    }
}