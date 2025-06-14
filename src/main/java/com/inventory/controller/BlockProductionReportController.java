package com.inventory.controller;

import com.inventory.model.BlockProduction;
import com.inventory.model.PithType;
import com.inventory.repository.BlockProductionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Sort;

import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/block-report")
@RequiredArgsConstructor
public class BlockProductionReportController extends BaseController {

        private final BlockProductionRepository blockProductionRepository;

        @GetMapping
        public String showReportPage(Model model) {
                return "redirect:/reports?tab=block";
        }

        @GetMapping("/view")
        public String viewReport(
                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                        @RequestParam(required = false) Boolean downloadCsv,
                        Model model,
                        HttpServletResponse response) {

                // Get all block productions between the dates
                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

                // Get all block productions sorted by production time
                List<BlockProduction> allProductions = blockProductionRepository.findAll(
                                Sort.by(Sort.Direction.DESC, "productionTime"));

                // Filter by date range
                List<BlockProduction> productions = allProductions.stream()
                                .filter(p -> p.getProductionTime() != null &&
                                                !p.getProductionTime().isBefore(startDateTime) &&
                                                !p.getProductionTime().isAfter(endDateTime))
                                .collect(Collectors.toList());

                // Group by pith type
                Map<PithType, List<BlockProduction>> productionsByType = productions.stream()
                                .collect(Collectors.groupingBy(BlockProduction::getPithType));

                // Calculate totals
                int totalNormalBlocks = productionsByType.getOrDefault(PithType.NORMAL, List.of()).stream()
                                .mapToInt(BlockProduction::getBlocksProduced)
                                .sum();

                int totalLowEcBlocks = productionsByType.getOrDefault(PithType.LOW, List.of()).stream()
                                .mapToInt(BlockProduction::getBlocksProduced)
                                .sum();

                // Handle CSV download if requested
                if (downloadCsv != null && downloadCsv) {
                        response.setContentType("text/csv");
                        response.setHeader("Content-Disposition",
                                        "attachment; filename=block_production_report_" + startDate + "_to_" + endDate + ".csv");

                        try (PrintWriter writer = response.getWriter()) {
                                // Write CSV header
                                writer.println("Date & Time,Block Type,Blocks Produced,Supervisor");

                                // Write data rows
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                                for (BlockProduction production : productions) {
                                        String dateTime = production.getProductionTime() != null 
                                                ? production.getProductionTime().format(formatter) : "";
                                        String blockType = production.getPithType() == PithType.NORMAL ? "Normal" : "Low EC";
                                        String blocksProduced = String.valueOf(production.getBlocksProduced());
                                        String supervisor = production.getSupervisorName() != null ? production.getSupervisorName() : "";

                                        writer.println(String.join(",",
                                                "\"" + dateTime + "\"",
                                                "\"" + blockType + "\"",
                                                blocksProduced,
                                                "\"" + supervisor + "\""));
                                }

                                // Write total row
                                writer.println("TOTAL,," + (totalNormalBlocks + totalLowEcBlocks) + ",");
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
                model.addAttribute("totalNormalBlocks", totalNormalBlocks);
                model.addAttribute("totalLowEcBlocks", totalLowEcBlocks);
                model.addAttribute("activeTab", "reports");
                model.addAttribute("activeReportTab", "block");

                return getViewPath("block-report/view");
        }
}