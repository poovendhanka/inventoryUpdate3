package com.inventory.controller;

import com.inventory.config.CompanyInfoProperties;
import com.inventory.model.ManualBill;
import com.inventory.model.ManualBillItem;
import com.inventory.service.ManualBillService;
import com.inventory.service.ManualBillPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/manual-billing")
public class ManualBillController {
    
    @Autowired
    private ManualBillService manualBillService;
    
    @Autowired
    private ManualBillPdfService manualBillPdfService;
    
    @GetMapping
    public String index(Model model) {
        model.addAttribute("bills", manualBillService.getRecentBills(10));
        return "manual-billing/index";
    }
    
    @GetMapping("/create")
    public String createBillForm(Model model) {
        model.addAttribute("bill", new ManualBill());
        model.addAttribute("companyInfo", manualBillService.getCompanyInfo());
        model.addAttribute("nextBillNumber", manualBillService.generateNextBillNumber());
        return "manual-billing/create";
    }
    
    @PostMapping("/create")
    public String createBill(@Valid @ModelAttribute ManualBill bill,
                           BindingResult bindingResult,
                           @RequestParam("itemDescription") List<String> itemDescriptions,
                           @RequestParam("hsnCode") List<String> hsnCodes,
                           @RequestParam("quantity") List<BigDecimal> quantities,
                           @RequestParam("unit") List<String> units,
                           @RequestParam("rate") List<BigDecimal> rates,
                           @RequestParam("taxType") List<String> taxTypes,
                           @RequestParam("taxRate") List<BigDecimal> taxRates,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("bill", bill);
            model.addAttribute("companyInfo", manualBillService.getCompanyInfo());
            model.addAttribute("nextBillNumber", manualBillService.generateNextBillNumber());
            return "manual-billing/create";
        }
        
        // Validate that at least one item is provided
        boolean hasValidItems = false;
        for (String desc : itemDescriptions) {
            if (desc != null && !desc.trim().isEmpty()) {
                hasValidItems = true;
                break;
            }
        }
        
        if (!hasValidItems) {
            bindingResult.reject("items.required", "At least one item is required");
            model.addAttribute("bill", bill);
            model.addAttribute("companyInfo", manualBillService.getCompanyInfo());
            model.addAttribute("nextBillNumber", manualBillService.generateNextBillNumber());
            return "manual-billing/create";
        }
        
        try {
            List<ManualBillItem> items = new ArrayList<>();
            
            for (int i = 0; i < itemDescriptions.size(); i++) {
                if (itemDescriptions.get(i) != null && !itemDescriptions.get(i).trim().isEmpty()) {
                    ManualBillItem item = new ManualBillItem();
                    item.setItemDescription(itemDescriptions.get(i));
                    item.setHsnCode(hsnCodes.get(i));
                    item.setQuantity(quantities.get(i));
                    item.setUnit(units.get(i));
                    item.setRate(rates.get(i));
                    item.setTaxType(taxTypes.get(i));
                    item.setTaxRate(taxRates.get(i));
                    items.add(item);
                }
            }
            
            ManualBill savedBill = manualBillService.createBill(bill, items);
            redirectAttributes.addFlashAttribute("successMessage", "Bill created successfully with number: " + savedBill.getBillNumber());
            return "redirect:/manual-billing";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating bill: " + e.getMessage());
            return "redirect:/manual-billing/create";
        }
    }
    
    @GetMapping("/view/{id}")
    public String viewBill(@PathVariable Long id, Model model) {
        Optional<ManualBill> billOpt = manualBillService.getBillById(id);
        if (billOpt.isEmpty()) {
            return "redirect:/manual-billing";
        }
        
        ManualBill bill = billOpt.get();
        List<ManualBillItem> items = manualBillService.getBillItems(id);
        
        model.addAttribute("bill", bill);
        model.addAttribute("items", items);
        model.addAttribute("companyInfo", manualBillService.getCompanyInfo());
        
        return "manual-billing/view";
    }
    

    
    @PostMapping("/delete/{id}")
    public String deleteBill(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            manualBillService.deleteBill(id);
            redirectAttributes.addFlashAttribute("successMessage", "Bill deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting bill: " + e.getMessage());
        }
        return "redirect:/manual-billing";
    }
    
    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<List<ManualBill>> searchBills(@RequestParam String customerName,
                                                       @RequestParam String startDate,
                                                       @RequestParam String endDate) {
        try {
            // Validate all parameters are provided
            if (customerName == null || customerName.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (startDate == null || startDate.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (endDate == null || endDate.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            // Validate date range
            if (end.isBefore(start)) {
                return ResponseEntity.badRequest().build();
            }
            
            List<ManualBill> bills = manualBillService.searchBillsByCustomerAndDateRange(customerName.trim(), start, end);
            return ResponseEntity.ok(bills);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/api/next-bill-number")
    @ResponseBody
    public ResponseEntity<String> getNextBillNumber() {
        return ResponseEntity.ok(manualBillService.generateNextBillNumber());
    }
    
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadBillPdf(@PathVariable Long id) {
        try {
            Optional<ManualBill> billOpt = manualBillService.getBillById(id);
            if (billOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            ManualBill bill = billOpt.get();
            List<ManualBillItem> items = manualBillService.getBillItems(id);
            
            byte[] pdfBytes = manualBillPdfService.generateBillPdf(bill, items);
            
            String filename = "bill_" + bill.getBillNumber().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
} 