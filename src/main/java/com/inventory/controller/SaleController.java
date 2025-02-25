package com.inventory.controller;

import com.inventory.model.Sale;
import com.inventory.model.ProductType;
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
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SaleController extends BaseController {

    private final SaleService saleService;
    private final DealerService dealerService;
    private final StockService stockService;

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
        return getViewPath("sales/index");
    }

    @PostMapping
    public String createSale(@ModelAttribute Sale sale, @RequestParam("dealer.id") Long dealerId) {
        // Set the dealer
        Dealer dealer = dealerService.getDealerById(dealerId);
        sale.setDealer(dealer);

        // Set the sale date
        sale.setSaleDate(LocalDateTime.now());

        // Calculate total amount
        if (sale.getQuantity() != null && sale.getPricePerUnit() != null) {
            sale.setTotalAmount(sale.getQuantity() * sale.getPricePerUnit());
        }

        // Create invoice number (you might want to implement your own logic)
        sale.setInvoiceNumber(generateInvoiceNumber());

        // Save the sale and update stock
        saleService.createSale(sale);

        return "redirect:/sales";
    }

    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis();
    }

    @GetMapping("/report")
    public String viewReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            Model model) {
        try {
            List<Sale> sales = saleService.getSalesByDate(date);
            Double totalAmount = sales.stream()
                    .mapToDouble(Sale::getTotalAmount)
                    .sum();

            model.addAttribute("date", date);
            model.addAttribute("sales", sales);
            model.addAttribute("totalAmount", totalAmount);
            return "sales/report";
        } catch (Exception e) {
            model.addAttribute("error", "Error generating report: " + e.getMessage());
            return "error";
        }
    }
}