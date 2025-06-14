package com.inventory.controller;

import com.inventory.model.*;
import com.inventory.service.SaleService;
import com.inventory.service.DealerService;
import com.inventory.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import com.inventory.model.PithType;
import com.inventory.model.FiberType;
import com.inventory.model.Dealer;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.UUID;
import com.inventory.service.BillPdfService;
import com.inventory.service.BillNumberService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.inventory.service.ProductCostService;

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
    public String showSalesPage(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate saleDate,
                               Model model, HttpServletRequest request) {
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

        if (saleDate != null) {
            // Show sales for specific date
            model.addAttribute("salesForDate", saleService.getSalesByDate(saleDate));
            model.addAttribute("selectedSaleDate", saleDate);
        } else {
            // Show recent sales (default behavior)
            model.addAttribute("recentSales", saleService.getRecentSales());
        }

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

            // Sale date and system time will be handled automatically by @PrePersist method
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