package com.inventory.controller;

import com.inventory.model.Employee;
import com.inventory.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/eyarkkai-family")
public class EyarkkaiController {
    
    @Autowired
    private EmployeeService employeeService;
    
    @GetMapping
    public String eyarkkaiFamily(@RequestParam(defaultValue = "about") String tab, 
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String employeeType,
                                Model model) {
        model.addAttribute("activeTab", "eyarkkai-family");
        model.addAttribute("currentTab", tab);
        
        // If employees tab is requested
        if ("employees".equals(tab)) {
            String selectedGender = (gender != null) ? gender : "male";
            model.addAttribute("selectedGender", selectedGender);
            model.addAttribute("employeeType", employeeType);
            
            if ("old".equals(employeeType)) {
                // Load old employees
                List<Employee> oldEmployees = employeeService.getDeletedEmployeesByGender(selectedGender.toUpperCase());
                model.addAttribute("oldEmployees", oldEmployees);
            } else {
                // Load active employees (default)
                List<Employee> employees = employeeService.getActiveEmployeesByGender(selectedGender.toUpperCase());
                model.addAttribute("employees", employees);
            }
        }
        
        return "eyarkkai-family/index";
    }
    
    @GetMapping("/employees")
    public String employees(@RequestParam(defaultValue = "male") String gender, Model model) {
        List<Employee> employees = employeeService.getActiveEmployeesByGender(gender.toUpperCase());
        model.addAttribute("activeTab", "eyarkkai-family");
        model.addAttribute("currentTab", "employees");
        model.addAttribute("selectedGender", gender);
        model.addAttribute("employees", employees);
        return "eyarkkai-family/index";
    }
    
    @GetMapping("/employees/add")
    public String addEmployeeForm(Model model) {
        System.out.println("DEBUG: Accessing add employee form");
        model.addAttribute("activeTab", "eyarkkai-family");
        model.addAttribute("employee", new Employee());
        return "eyarkkai-family/employee-form";
    }
    
    @PostMapping("/employees/add")
    public String addEmployee(@Valid @ModelAttribute Employee employee, 
                             BindingResult bindingResult, 
                             RedirectAttributes redirectAttributes,
                             Model model) {
        
        System.out.println("DEBUG: POST request received for adding employee");
        System.out.println("DEBUG: Employee data: " + employee.getName());
        
        if (bindingResult.hasErrors()) {
            System.out.println("DEBUG: Validation errors found");
            bindingResult.getAllErrors().forEach(error -> 
                System.out.println("DEBUG: Error: " + error.getDefaultMessage())
            );
            model.addAttribute("activeTab", "eyarkkai-family");
            return "eyarkkai-family/employee-form";
        }
        
        if (employeeService.existsByAadharNumberAmongActive(employee.getAadharNumber())) {
            System.out.println("DEBUG: Aadhar number already exists among active employees");
            bindingResult.rejectValue("aadharNumber", "error.employee", "Aadhar number already exists");
            model.addAttribute("activeTab", "eyarkkai-family");
            return "eyarkkai-family/employee-form";
        }
        
        try {
            employeeService.saveEmployee(employee);
            System.out.println("DEBUG: Employee saved successfully");
            redirectAttributes.addFlashAttribute("successMessage", "Employee added successfully!");
            return "redirect:/eyarkkai-family?tab=employees&gender=" + employee.getGender().toLowerCase();
        } catch (Exception e) {
            System.err.println("DEBUG: Error saving employee: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("activeTab", "eyarkkai-family");
            model.addAttribute("errorMessage", "Error saving employee: " + e.getMessage());
            return "eyarkkai-family/employee-form";
        }
    }
    
    @GetMapping("/employees/{id}")
    public String viewEmployee(@PathVariable Long id, Model model) {
        Optional<Employee> employee = employeeService.getActiveEmployeeById(id);
        if (employee.isPresent()) {
            model.addAttribute("activeTab", "eyarkkai-family");
            model.addAttribute("employee", employee.get());
            model.addAttribute("isOldEmployee", false);
            return "eyarkkai-family/employee-details";
        }
        return "redirect:/eyarkkai-family?tab=employees";
    }
    
    @GetMapping("/old-employees/{id}")
    public String viewOldEmployee(@PathVariable Long id, Model model) {
        Optional<Employee> employee = employeeService.getAnyEmployeeById(id);
        if (employee.isPresent() && Boolean.TRUE.equals(employee.get().getDeleted())) {
            model.addAttribute("activeTab", "eyarkkai-family");
            model.addAttribute("employee", employee.get());
            model.addAttribute("isOldEmployee", true);
            return "eyarkkai-family/employee-details";
        }
        return "redirect:/eyarkkai-family?tab=employees&employeeType=old";
    }
    
    @GetMapping("/employees/{id}/edit")
    public String editEmployeeForm(@PathVariable Long id, Model model) {
        Optional<Employee> employee = employeeService.getActiveEmployeeById(id);
        if (employee.isPresent()) {
            model.addAttribute("activeTab", "eyarkkai-family");
            model.addAttribute("employee", employee.get());
            return "eyarkkai-family/employee-form";
        }
        return "redirect:/eyarkkai-family?tab=employees";
    }
    
    @PostMapping("/employees/{id}/edit")
    public String editEmployee(@PathVariable Long id,
                              @Valid @ModelAttribute Employee employee, 
                              BindingResult bindingResult, 
                              RedirectAttributes redirectAttributes,
                              Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeTab", "eyarkkai-family");
            return "eyarkkai-family/employee-form";
        }
        
        if (!employeeService.isAadharNumberUniqueAmongActive(employee.getAadharNumber(), id)) {
            bindingResult.rejectValue("aadharNumber", "error.employee", "Aadhar number already exists");
            model.addAttribute("activeTab", "eyarkkai-family");
            return "eyarkkai-family/employee-form";
        }
        
        employee.setId(id);
        employeeService.saveEmployee(employee);
        redirectAttributes.addFlashAttribute("successMessage", "Employee updated successfully!");
        return "redirect:/eyarkkai-family?tab=employees&gender=" + employee.getGender().toLowerCase();
    }
    
    @PostMapping("/employees/{id}/delete")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Employee> employee = employeeService.getActiveEmployeeById(id);
        if (employee.isPresent()) {
            String gender = employee.get().getGender().toLowerCase();
            employeeService.softDeleteEmployee(id, "Admin"); // You can pass actual user info here
            redirectAttributes.addFlashAttribute("successMessage", 
                "Employee moved to old employees successfully!");
            return "redirect:/eyarkkai-family?tab=employees&gender=" + gender;
        }
        return "redirect:/eyarkkai-family?tab=employees";
    }
    
    @PostMapping("/old-employees/{id}/restore")
    public String restoreEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Employee> employee = employeeService.getAnyEmployeeById(id);
        if (employee.isPresent() && Boolean.TRUE.equals(employee.get().getDeleted())) {
            String gender = employee.get().getGender().toLowerCase();
            employeeService.restoreEmployee(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Employee restored successfully!");
            return "redirect:/eyarkkai-family?tab=employees&gender=" + gender;
        }
        return "redirect:/eyarkkai-family?tab=employees&employeeType=old";
    }
    
    @GetMapping("/connect")
    public String connect(Model model) {
        model.addAttribute("activeTab", "eyarkkai-family");
        model.addAttribute("currentTab", "connect");
        return "eyarkkai-family/index";
    }
} 