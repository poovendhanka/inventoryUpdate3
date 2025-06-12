package com.inventory.controller;

import com.inventory.model.Sale;
import com.inventory.model.ProductType;
import com.inventory.service.SaleService;
import com.inventory.service.DealerService;
import com.inventory.service.StockService;
import com.inventory.util.ProductNameUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import com.inventory.model.PithType;
import com.inventory.model.FiberType;
import com.inventory.model.Dealer;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.UUID;
import com.inventory.service.BillPdfService;
import com.inventory.service.BillNumberService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.inventory.service.ProductCostService;
import com.inventory.model.ProductCost;

@Controller
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SaleController extends BaseController {

    private final SaleService saleService;
    private final DealerService dealerService;
    private final StockService stockService;
    private final BillPdfService billPdfService;
    private final BillNumberService billNumberService;
    private final ProductCostService productCostService;

    @GetMapping
    public String showSalesPage(Model model, HttpServletRequest request) {
        model.addAttribute("activeTab", "sales");
        model.addAttribute("sale", new Sale());
        model.addAttribute("dealers", dealerService.getAllDealers());
        model.addAttribute("productTypes", ProductType.values());
        model.addAttribute("pithTypes", PithType.values());
        model.addAttribute("fiberTypes", FiberType.values());

        // Add all stock types for validation
        model.addAttribute("currentPithStock", stockService.getCurrentPithStock());
        model.addAttribute("currentLowEcPithStock", stockService.getCurrentLowEcPithStock());
        model.addAttribute("whiteFiberStock", stockService.getCurrentFiberStock(FiberType.WHITE));
        model.addAttribute("brownFiberStock", stockService.getCurrentFiberStock(FiberType.BROWN));
        model.addAttribute("blockStock", stockService.getCurrentBlockStock());
        model.addAttribute("normalBlockStock", stockService.getCurrentBlockStock(PithType.NORMAL));
        model.addAttribute("lowEcBlockStock", stockService.getCurrentBlockStock(PithType.LOW));

        model.addAttribute("recentSales", saleService.getRecentSales());
        model.addAttribute("productCost", productCostService.getCurrentCost());
        return getViewPath("sales/index");
    }

    @PostMapping
    public String createSale(@ModelAttribute Sale sale, @RequestParam("dealer.id") Long dealerId,
            RedirectAttributes redirectAttributes) {
        try {
            // Validate input
            if (sale.getQuantity() == null || sale.getQuantity() <= 0) {
                redirectAttributes.addFlashAttribute("error", "Quantity must be greater than zero");
                return "redirect:/sales";
            }

            if (sale.getPricePerUnit() == null || sale.getPricePerUnit() <= 0) {
                redirectAttributes.addFlashAttribute("error", "Price must be greater than zero");
                return "redirect:/sales";
            }

            // Set the dealer
            Dealer dealer = dealerService.getDealerById(dealerId);
            if (dealer == null) {
                redirectAttributes.addFlashAttribute("error", "Invalid dealer selected");
                return "redirect:/sales";
            }
            sale.setDealer(dealer);

            // Check if sufficient stock is available
            boolean hasStock = false;
            switch (sale.getProductType()) {
                case PITH:
                    if (sale.getPithType() == PithType.LOW) {
                        hasStock = stockService.getCurrentLowEcPithStock() >= sale.getQuantity();
                    } else {
                        hasStock = stockService.getCurrentPithStock() >= sale.getQuantity();
                    }
                    break;
                case FIBER:
                    if (sale.getFiberType() == FiberType.WHITE) {
                        hasStock = stockService.getCurrentFiberStock(FiberType.WHITE) >= sale.getQuantity();
                    } else {
                        hasStock = stockService.getCurrentFiberStock(FiberType.BROWN) >= sale.getQuantity();
                    }
                    break;
                case BLOCK:
                    if (sale.getPithType() == PithType.LOW) {
                        hasStock = stockService.getCurrentBlockStock(PithType.LOW) >= sale.getQuantity();
                    } else {
                        hasStock = stockService.getCurrentBlockStock(PithType.NORMAL) >= sale.getQuantity();
                    }
                    break;
            }

            if (!hasStock) {
                redirectAttributes.addFlashAttribute("error", "Insufficient stock available for this sale");
                return "redirect:/sales";
            }

            // Set the sale date
            sale.setSaleDate(LocalDateTime.now());

            // Total amount and tax calculations will be handled by @PrePersist method

            // Create invoice number with better uniqueness
            sale.setInvoiceNumber(generateInvoiceNumber());

            // Save the sale and update stock
            saleService.createSale(sale);

            String productDescription = sale.getProductType().name();
            if (sale.getPithType() != null) {
                productDescription = sale.getPithType().getDisplayName();
            } else if (sale.getFiberType() != null) {
                productDescription = sale.getFiberType().getDisplayName() + " Fiber";
            }
            
            redirectAttributes.addFlashAttribute("success", 
                "Sale completed successfully! Invoice #" + sale.getInvoiceNumber() + 
                " - " + sale.getQuantity() + " units of " + productDescription + 
                " sold to " + sale.getDealer().getName() + " for ₹" + sale.getTotalWithTax());
            return "redirect:/sales";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error processing sale: " + e.getMessage());
            return "redirect:/sales";
        }
    }

    private String generateInvoiceNumber() {
        // Format: INV-{yyyy-MM-dd}-{UUID-first-8-chars}
        LocalDate today = LocalDate.now();
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("INV-%s-%s", today.toString(), uuid);
    }

    @GetMapping("/report")
    public String viewReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
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

            List<Sale> sales = saleService.getSalesByDateRange(fromDate, toDate);
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
            model.addAttribute("sales", sales);
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("activeTab", "sales");
            return "sales/report";
        } catch (Exception e) {
            model.addAttribute("error", "Error generating report: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/{id}/bill")
    public ResponseEntity<byte[]> downloadBill(@PathVariable Long id) {
        try {
            Sale sale = saleService.getSaleById(id);
            if (sale == null) {
                return ResponseEntity.notFound().build();
            }
            Dealer dealer = sale.getDealer();
            String billNumber = sale.getInvoiceNumber();
            byte[] pdfBytes = billPdfService.generateBillPdf(sale, dealer, billNumber);
            String filename = "Bill-" + billNumber + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}