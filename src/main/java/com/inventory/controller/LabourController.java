package com.inventory.controller;

import com.inventory.model.LabourEntry;
import com.inventory.model.Employee;
import com.inventory.service.LabourEntryService;
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
    private final EmployeeService employeeService;

    @GetMapping("/entries")
    public String showLabourEntriesPage(Model model) {
        model.addAttribute("activeTab", "expenses");
        model.addAttribute("activeSubTab", "labour");
        model.addAttribute("isEdit", false);

        // Create a new labour entry
        LabourEntry labourEntry = new LabourEntry();
        labourEntry.setWorkDate(LocalDate.now());
        model.addAttribute("labourEntry", labourEntry);

        // Get recent labour entries
        model.addAttribute("recentLabourEntries", labourEntryService.getRecentLabourEntries(10));

        return getViewPath("expenses/labour-entries");
    }

    @PostMapping("/entries/save")
    public String saveLabourEntry(@Valid @ModelAttribute LabourEntry labourEntry, 
                                  BindingResult bindingResult, 
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("activeTab", "expenses");
                model.addAttribute("activeSubTab", "labour");
                model.addAttribute("recentLabourEntries", labourEntryService.getRecentLabourEntries(10));
                return getViewPath("expenses/labour-entries");
            }

            // Get the employee by ID from the form
            Long employeeId = labourEntry.getEmployee().getId();
            Optional<Employee> employeeOpt = employeeService.getEmployeeById(employeeId);
            
            if (employeeOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Selected employee not found");
                return "redirect:/expenses/labour/entries";
            }

            labourEntry.setEmployee(employeeOpt.get());
            labourEntryService.saveLabourEntry(labourEntry);
            
            redirectAttributes.addFlashAttribute("success", 
                "Labour entry saved successfully! " + labourEntry.getEmployee().getName() + 
                " - " + labourEntry.getHoursWorked() + " hours on " + labourEntry.getWorkDate() + 
                " (Total: ₹" + labourEntry.getTotalCost() + ")");
            log.info("Labour entry saved for employee: {} on date: {}", 
                    labourEntry.getEmployee().getName(), labourEntry.getWorkDate());
            
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
            List<Employee> employees = employeeService.getEmployeesByGender(gender.toUpperCase());
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
            Optional<Employee> employeeOpt = employeeService.getEmployeeById(employeeId);
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
} 