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
                                Model model) {
        model.addAttribute("activeTab", "eyarkkai-family");
        model.addAttribute("currentTab", tab);
        
        // If employees tab is requested, load employees
        if ("employees".equals(tab)) {
            String selectedGender = (gender != null) ? gender : "male";
            List<Employee> employees = employeeService.getEmployeesByGender(selectedGender.toUpperCase());
            model.addAttribute("selectedGender", selectedGender);
            model.addAttribute("employees", employees);
        }
        
        return "eyarkkai-family/index";
    }
    
    @GetMapping("/employees")
    public String employees(@RequestParam(defaultValue = "male") String gender, Model model) {
        List<Employee> employees = employeeService.getEmployeesByGender(gender.toUpperCase());
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
        
        if (employeeService.existsByAadharNumber(employee.getAadharNumber())) {
            System.out.println("DEBUG: Aadhar number already exists");
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
        Optional<Employee> employee = employeeService.getEmployeeById(id);
        if (employee.isPresent()) {
            model.addAttribute("activeTab", "eyarkkai-family");
            model.addAttribute("employee", employee.get());
            return "eyarkkai-family/employee-details";
        }
        return "redirect:/eyarkkai-family?tab=employees";
    }
    
    @GetMapping("/employees/{id}/edit")
    public String editEmployeeForm(@PathVariable Long id, Model model) {
        Optional<Employee> employee = employeeService.getEmployeeById(id);
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
        
        if (!employeeService.isAadharNumberUnique(employee.getAadharNumber(), id)) {
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
        Optional<Employee> employee = employeeService.getEmployeeById(id);
        if (employee.isPresent()) {
            String gender = employee.get().getGender().toLowerCase();
            employeeService.deleteEmployee(id);
            redirectAttributes.addFlashAttribute("successMessage", "Employee deleted successfully!");
            return "redirect:/eyarkkai-family?tab=employees&gender=" + gender;
        }
        return "redirect:/eyarkkai-family?tab=employees";
    }
    
    @GetMapping("/connect")
    public String connect(Model model) {
        model.addAttribute("activeTab", "eyarkkai-family");
        model.addAttribute("currentTab", "connect");
        return "eyarkkai-family/index";
    }
} 