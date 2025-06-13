package com.inventory.controller;

import com.inventory.model.Sale;
import com.inventory.service.SaleService;
import com.inventory.util.ProductNameUtil;
import com.inventory.model.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/sales-report")
@RequiredArgsConstructor
public class SalesReportController extends BaseController {

    private final SaleService saleService;

    @GetMapping
    public String showReportPage(Model model) {
        return "redirect:/reports?tab=sales";
    }

    @GetMapping("/view")
    public String viewReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) Long dealerId,
            @RequestParam(defaultValue = "false") boolean exportCsv,
            Model model,
            HttpServletResponse response) throws IOException {
        try {
            // Handle legacy single date param
            if (date != null && fromDate == null && toDate == null) {
                fromDate = date;
                toDate = date;
            }

            // If dates are not provided, use current date as default
            if (fromDate == null) {
                fromDate = LocalDate.now();
            }
            if (toDate == null) {
                toDate = LocalDate.now();
            }

            List<Sale> sales = saleService.getSalesByDateRangeWithFilters(fromDate, toDate, productType, dealerId);
            Double totalAmount = sales.stream()
                    .mapToDouble(sale -> sale.getTotalWithTax() != null ? sale.getTotalWithTax() : 0.0)
                    .sum();

            // Handle CSV export if requested
            if (exportCsv) {
                response.setContentType("text/csv");
                response.setHeader("Content-Disposition",
                        "attachment; filename=sales_report_" + fromDate + "_to_" + toDate + ".csv");

                try (PrintWriter writer = response.getWriter()) {
                    // Write CSV header
                    writer.println("Date & Time,Dealer,Product,Quantity,Price Per Unit,Tax Amount,Total Amount");

                    // Write data rows
                    for (Sale sale : sales) {
                        String product = ProductNameUtil.getFullProductName(sale.getProductType(), sale.getPithType(),
                                sale.getFiberType());
                        String quantity = "";
                        if (sale.getProductType() == ProductType.BLOCK) {
                            quantity = sale.getBlockCount() + " blocks";
                        } else if (sale.getProductType() == ProductType.PITH) {
                            quantity = sale.getQuantity() + " kg";
                        } else if (sale.getProductType() == ProductType.FIBER) {
                            quantity = sale.getQuantity() + " bales";
                        }

                        writer.println(String.join(",",
                                "\"" + sale.getSaleDate()
                                        .format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
                                        + "\"",
                                "\"" + sale.getDealer().getName() + "\"",
                                "\"" + product + "\"",
                                "\"" + quantity + "\"",
                                "₹" + sale.getPricePerUnit(),
                                "₹" + (sale.getTaxAmount() != null ? sale.getTaxAmount() : 0.0),
                                "₹" + (sale.getTotalWithTax() != null ? sale.getTotalWithTax() : 0.0)));
                    }

                    // Write total row
                    writer.println(",,,,,,\"₹" + totalAmount + "\"");
                }
                return null;
            }

            model.addAttribute("fromDate", fromDate);
            model.addAttribute("toDate", toDate);
            model.addAttribute("productType", productType);
            model.addAttribute("dealerId", dealerId);
            model.addAttribute("sales", sales);
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("activeTab", "reports");
            model.addAttribute("activeReportTab", "sales");
            return getViewPath("sales-report/view");
        } catch (Exception e) {
            model.addAttribute("error", "Error generating report: " + e.getMessage());
            return "error";
        }
    }
} 