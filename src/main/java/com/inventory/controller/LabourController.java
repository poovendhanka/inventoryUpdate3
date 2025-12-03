package com.inventory.controller;

import com.inventory.model.LabourEntry;
import com.inventory.model.Employee;
import com.inventory.service.LabourEntryService;
import com.inventory.service.LabourAdvanceService;
import com.inventory.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/expenses/labour")
@RequiredArgsConstructor
public class LabourController extends BaseController {

    private final LabourEntryService labourEntryService;
    private final LabourAdvanceService labourAdvanceService;
    private final EmployeeService employeeService;

    @GetMapping("/entries")
    public String showLabourEntriesPage(Model model) {
        model.addAttribute("activeTab", "expenses");
        model.addAttribute("activeSubTab", "labour");
        model.addAttribute("isEdit", false);

        // Create a new labour entry
        LabourEntry labourEntry = new LabourEntry();
        LocalDate today = LocalDate.now();
        labourEntry.setFromDate(today);
        labourEntry.setToDate(today);
        model.addAttribute("labourEntry", labourEntry);

        // Get recent labour entries
        model.addAttribute("recentLabourEntries", labourEntryService.getRecentLabourEntries(10));

        // Get all active employees for advance form
        model.addAttribute("employees", employeeService.getAllActiveEmployees());

        return getViewPath("expenses/labour-entries");
    }

    @PostMapping("/entries/save")
    public String saveLabourEntry(@Valid @ModelAttribute LabourEntry labourEntry, 
                                  @RequestParam(defaultValue = "false") boolean applyAdvance,
                                  @RequestParam(required = false) Double customAdvanceDeduction,
                                  BindingResult bindingResult, 
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("activeTab", "expenses");
                model.addAttribute("activeSubTab", "labour");
                model.addAttribute("recentLabourEntries", labourEntryService.getRecentLabourEntries(10));
                model.addAttribute("employees", employeeService.getAllActiveEmployees());
                return getViewPath("expenses/labour-entries");
            }

            // Get the employee by ID from the form
            Long employeeId = labourEntry.getEmployee().getId();
            Optional<Employee> employeeOpt = employeeService.getActiveEmployeeById(employeeId);
            
            if (employeeOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Selected employee not found");
                return "redirect:/expenses/labour/entries";
            }

            labourEntry.setEmployee(employeeOpt.get());
            
            // Save with advance adjustment if requested
            labourEntryService.saveLabourEntryWithAdvance(labourEntry, applyAdvance, customAdvanceDeduction);
            
            String dateRangeStr = labourEntry.getFromDate().equals(labourEntry.getToDate()) 
                ? labourEntry.getFromDate().toString() 
                : labourEntry.getFromDate().toString() + " to " + labourEntry.getToDate().toString();
            
            String successMessage = "Labour entry saved successfully! " + labourEntry.getEmployee().getName() +
                " - " + labourEntry.getHoursWorked() + " hours from " + dateRangeStr +
                " (Total: ₹" + String.format("%.2f", labourEntry.getTotalCost()) + ")";

            double advanceAdjustmentValue = labourEntry.getAdvanceAdjustment() != null
                    ? labourEntry.getAdvanceAdjustment().doubleValue()
                    : 0.0;
            double netPayableValue = labourEntry.getNetPayable() != null
                    ? labourEntry.getNetPayable().doubleValue()
                    : 0.0;

            if (applyAdvance && advanceAdjustmentValue > 0) {
                successMessage += " | Advance Adjusted: ₹" + String.format("%.2f", advanceAdjustmentValue) +
                    " | Net Payable: ₹" + String.format("%.2f", netPayableValue);
            } else if (applyAdvance) {
                successMessage += " | No outstanding advance to adjust";
            }
            
            redirectAttributes.addFlashAttribute("success", successMessage);
            log.info("Labour entry saved for employee: {} from {} to {} with advance adjustment: {}", 
                    labourEntry.getEmployee().getName(), labourEntry.getFromDate(), labourEntry.getToDate(), 
                    labourEntry.getAdvanceAdjustment());
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Error saving labour entry", e);
            redirectAttributes.addFlashAttribute("error", "Error saving labour entry: " + e.getMessage());
        }

        return "redirect:/expenses/labour/entries";
    }

    @GetMapping("/entries/edit/{id}")
    public String editLabourEntry(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<LabourEntry> labourEntryOpt = labourEntryService.getLabourEntryById(id);
        
        if (labourEntryOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Labour entry not found");
            return "redirect:/expenses/labour/entries";
        }

        model.addAttribute("activeTab", "expenses");
        model.addAttribute("activeSubTab", "labour");
        model.addAttribute("labourEntry", labourEntryOpt.get());
        model.addAttribute("recentLabourEntries", labourEntryService.getRecentLabourEntries(10));
        model.addAttribute("employees", employeeService.getAllActiveEmployees());
        model.addAttribute("isEdit", true);

        return getViewPath("expenses/labour-entries");
    }

    @GetMapping("/entries/delete/{id}")
    public String deleteLabourEntry(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<LabourEntry> labourEntryOpt = labourEntryService.getLabourEntryById(id);
            if (labourEntryOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Labour entry not found");
            } else {
                labourEntryService.deleteLabourEntry(id);
                redirectAttributes.addFlashAttribute("success", "Labour entry deleted successfully");
                log.info("Deleted labour entry with ID: {}", id);
            }
        } catch (Exception e) {
            log.error("Error deleting labour entry with ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Error deleting labour entry: " + e.getMessage());
        }

        return "redirect:/expenses/labour/entries";
    }

    // API endpoint to get employees by gender
    @GetMapping("/api/employees/{gender}")
    @ResponseBody
    public ResponseEntity<List<Employee>> getEmployeesByGender(@PathVariable String gender) {
        try {
            List<Employee> employees = employeeService.getActiveEmployeesByGender(gender.toUpperCase());
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            log.error("Error fetching employees by gender: {}", gender, e);
            return ResponseEntity.badRequest().build();
        }
    }

    // API endpoint to check if employee has entry for date
    @GetMapping("/api/check-entry")
    @ResponseBody
    public ResponseEntity<Boolean> checkExistingEntry(@RequestParam Long employeeId, 
                                                      @RequestParam String date) {
        try {
            Optional<Employee> employeeOpt = employeeService.getActiveEmployeeById(employeeId);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            LocalDate workDate = LocalDate.parse(date);
            boolean exists = labourEntryService.hasLabourEntryForEmployeeOnDate(employeeOpt.get(), workDate);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            log.error("Error checking existing labour entry", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Record advance for employee
    @PostMapping("/advances")
    public String recordAdvance(@RequestParam Long employeeId,
                                @RequestParam Double amount,
                                @RequestParam(required = false) String advanceDate,
                                @RequestParam(required = false) String remarks,
                                RedirectAttributes redirectAttributes) {
        try {
            Optional<Employee> employeeOpt = employeeService.getActiveEmployeeById(employeeId);
            if (employeeOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Employee not found");
                return "redirect:/expenses/labour/entries";
            }

            if (amount == null || amount <= 0) {
                redirectAttributes.addFlashAttribute("error", "Advance amount must be greater than 0");
                return "redirect:/expenses/labour/entries";
            }

            LocalDate advDate = advanceDate != null && !advanceDate.trim().isEmpty() 
                ? LocalDate.parse(advanceDate) 
                : LocalDate.now();

            labourAdvanceService.recordAdvance(employeeOpt.get(), amount, advDate, remarks);
            
            redirectAttributes.addFlashAttribute("success", 
                "Advance of ₹" + String.format("%.2f", amount) + " recorded for " + employeeOpt.get().getName());
            log.info("Advance recorded: ₹{} for employee: {}", amount, employeeOpt.get().getName());
            
        } catch (Exception e) {
            log.error("Error recording advance", e);
            redirectAttributes.addFlashAttribute("error", "Error recording advance: " + e.getMessage());
        }

        return "redirect:/expenses/labour/entries";
    }

    // API endpoint to get outstanding advance for employee
    @GetMapping("/api/outstanding-advance/{employeeId}")
    @ResponseBody
    public ResponseEntity<Double> getOutstandingAdvance(@PathVariable Long employeeId) {
        try {
            Optional<Employee> employeeOpt = employeeService.getActiveEmployeeById(employeeId);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            double outstanding = labourAdvanceService.getOutstandingAdvance(employeeOpt.get());
            return ResponseEntity.ok(outstanding);
        } catch (Exception e) {
            log.error("Error getting outstanding advance", e);
            return ResponseEntity.badRequest().build();
        }
    }
} 