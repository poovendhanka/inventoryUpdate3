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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
                model.addAttribute("activeTab", "block-report");
                return getViewPath("block-report/index");
        }

        @GetMapping("/view")
        public String viewReport(
                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                        Model model) {

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

                double totalNormalPithUsed = totalNormalBlocks * 5.0; // 5kg per block
                double totalLowEcPithUsed = totalLowEcBlocks * 5.0;

                // Add data to model
                model.addAttribute("productions", productions);
                model.addAttribute("startDate", startDate);
                model.addAttribute("endDate", endDate);
                model.addAttribute("totalNormalBlocks", totalNormalBlocks);
                model.addAttribute("totalLowEcBlocks", totalLowEcBlocks);
                model.addAttribute("totalNormalPithUsed", totalNormalPithUsed);
                model.addAttribute("totalLowEcPithUsed", totalLowEcPithUsed);
                model.addAttribute("activeTab", "block-report");

                return getViewPath("block-report/view");
        }
}