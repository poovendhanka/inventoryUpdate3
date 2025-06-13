package com.inventory.controller;

import com.inventory.model.Expense;
import com.inventory.model.ExpenseType;
import com.inventory.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController extends BaseController {

    private final ExpenseService expenseService;

    @GetMapping
    public String showExpensesPage(Model model) {
        model.addAttribute("activeTab", "expenses");
        // Default redirect to electricity bills subtab
        return "redirect:/expenses/electricity";
    }

    @GetMapping("/electricity")
    public String showElectricityExpensesPage(Model model) {
        model.addAttribute("activeTab", "expenses");
        model.addAttribute("activeSubTab", "electricity");

        // Create a new expense with pre-filled type
        Expense expense = new Expense();
        expense.setExpenseType(ExpenseType.ELECTRICITY_BILLS);
        model.addAttribute("expense", expense);

        model.addAttribute("expenseType", ExpenseType.ELECTRICITY_BILLS);
        model.addAttribute("recentExpenses", expenseService.getRecentExpensesByType(ExpenseType.ELECTRICITY_BILLS));
        return getViewPath("expenses/electricity");
    }

    @GetMapping("/labour")
    public String showLabourExpensesPage(Model model) {
        // Redirect to the new labour entries system
        return "redirect:/expenses/labour/entries";
    }

    @GetMapping("/maintenance")
    public String showMaintenanceExpensesPage(Model model) {
        model.addAttribute("activeTab", "expenses");
        model.addAttribute("activeSubTab", "maintenance");

        // Create a new expense with pre-filled type
        Expense expense = new Expense();
        expense.setExpenseType(ExpenseType.MAINTENANCE);
        model.addAttribute("expense", expense);

        model.addAttribute("expenseType", ExpenseType.MAINTENANCE);
        model.addAttribute("recentExpenses", expenseService.getRecentExpensesByType(ExpenseType.MAINTENANCE));
        return getViewPath("expenses/maintenance");
    }

    @GetMapping("/transportation")
    public String showTransportationExpensesPage(Model model) {
        model.addAttribute("activeTab", "expenses");
        model.addAttribute("activeSubTab", "transportation");

        // Create a new expense with pre-filled type
        Expense expense = new Expense();
        expense.setExpenseType(ExpenseType.TRANSPORTATION);
        model.addAttribute("expense", expense);

        model.addAttribute("expenseType", ExpenseType.TRANSPORTATION);
        model.addAttribute("recentExpenses", expenseService.getRecentExpensesByType(ExpenseType.TRANSPORTATION));
        return getViewPath("expenses/transportation");
    }

    @GetMapping("/spares")
    public String showSparesExpensesPage(Model model) {
        model.addAttribute("activeTab", "expenses");
        model.addAttribute("activeSubTab", "spares");

        // Create a new expense with pre-filled type
        Expense expense = new Expense();
        expense.setExpenseType(ExpenseType.SPARES_PARTS);
        model.addAttribute("expense", expense);

        model.addAttribute("expenseType", ExpenseType.SPARES_PARTS);
        model.addAttribute("recentExpenses", expenseService.getRecentExpensesByType(ExpenseType.SPARES_PARTS));
        return getViewPath("expenses/spares");
    }

    @GetMapping("/miscellaneous")
    public String showMiscellaneousExpensesPage(Model model) {
        model.addAttribute("activeTab", "expenses");
        model.addAttribute("activeSubTab", "miscellaneous");

        // Create a new expense with pre-filled type
        Expense expense = new Expense();
        expense.setExpenseType(ExpenseType.MISCELLANEOUS);
        model.addAttribute("expense", expense);

        model.addAttribute("expenseType", ExpenseType.MISCELLANEOUS);
        model.addAttribute("recentExpenses", expenseService.getRecentExpensesByType(ExpenseType.MISCELLANEOUS));
        return getViewPath("expenses/miscellaneous");
    }

    @PostMapping("/save")
    public String saveExpense(@ModelAttribute Expense expense, RedirectAttributes redirectAttributes) {
        try {
            if (expense.getAmount() == null || expense.getAmount() <= 0) {
                redirectAttributes.addFlashAttribute("error", "Amount must be greater than zero");
                return getRedirectUrl(expense.getExpenseType());
            }

            // Ensure expense type is not null
            if (expense.getExpenseType() == null) {
                redirectAttributes.addFlashAttribute("error", "Expense type must be selected");
                return "redirect:/expenses";
            }

            expense.setEntryDate(LocalDateTime.now());
            if (expense.getExpenseDate() == null) {
                expense.setExpenseDate(LocalDateTime.now());
            }

            expenseService.saveExpense(expense);
            redirectAttributes.addFlashAttribute("success", 
                "Expense saved successfully! ₹" + expense.getAmount() + " " + 
                expense.getExpenseType().getDisplayName() + " expense recorded.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving expense: " + e.getMessage());
        }

        return getRedirectUrl(expense.getExpenseType());
    }

    @GetMapping("/report")
    public String viewReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
            @RequestParam(required = false) ExpenseType expenseType,
            @RequestParam(defaultValue = "false") boolean exportCsv,
            Model model,
            HttpServletResponse response) throws IOException {

        // If dates are not provided, use current date as default
        if (fromDate == null) {
            fromDate = LocalDate.now();
        }
        if (toDate == null) {
            toDate = LocalDate.now();
        }

        List<Expense> expenses;
        Double totalAmount;

        if (expenseType == null) {
            expenses = expenseService.getExpensesByDateRange(fromDate, toDate);
            totalAmount = expenseService.getTotalExpenseAmountByDateRange(fromDate, toDate);
            model.addAttribute("reportTitle", "All Expenses Report");
        } else {
            expenses = expenseService.getExpensesByTypeAndDateRange(expenseType, fromDate, toDate);
            totalAmount = expenseService.getTotalExpenseAmountByTypeAndDateRange(expenseType, fromDate, toDate);
            model.addAttribute("reportTitle", expenseType.getDisplayName() + " Expenses Report");
        }

        // Handle CSV export if requested
        if (exportCsv) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition",
                    "attachment; filename=expenses_report_" + fromDate + "_to_" + toDate + ".csv");

            try (PrintWriter writer = response.getWriter()) {
                // Write CSV header - adjust fields based on expense type
                String header = "Date & Time,Expense Type,Description,Amount";
                if (expenseType == ExpenseType.ELECTRICITY_BILLS) {
                    header += ",Units Consumed";
                } else if (expenseType == ExpenseType.LABOUR) {
                    header += ",Men Count,Women Count,Shift";
                } else if (expenseType == ExpenseType.MAINTENANCE) {
                    header += ",Maintenance Type";
                } else if (expenseType == ExpenseType.TRANSPORTATION) {
                    header += ",Fuel Quantity,Vehicle Details";
                } else if (expenseType == ExpenseType.SPARES_PARTS) {
                    header += ",Part Name,Replacement Details";
                }

                writer.println(header);

                // Write data rows
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                for (Expense expense : expenses) {
                    StringBuilder line = new StringBuilder();
                    line.append("\"").append(expense.getExpenseDate().format(dateFormatter)).append("\",");

                    // Safely handle expense type
                    ExpenseType type = expense.getExpenseType();
                    line.append("\"").append(type != null ? type.getDisplayName() : "Unknown").append("\",");

                    line.append("\"").append(
                            expense.getDescription() != null ? expense.getDescription().replace("\"", "\"\"") : "")
                            .append("\",");
                    line.append("\"").append(expense.getAmount()).append("\"");

                    // Add type-specific fields
                    if (expense.getExpenseType() == ExpenseType.ELECTRICITY_BILLS
                            && expense.getUnitsConsumed() != null) {
                        line.append(",\"").append(expense.getUnitsConsumed()).append("\"");
                    } else if (expense.getExpenseType() == ExpenseType.LABOUR) {
                        line.append(",\"").append(expense.getMenCount() != null ? expense.getMenCount() : 0)
                                .append("\"");
                        line.append(",\"").append(expense.getWomenCount() != null ? expense.getWomenCount() : 0)
                                .append("\"");
                        line.append(",\"").append(expense.getShift() != null ? expense.getShift() : "").append("\"");
                    } else if (expense.getExpenseType() == ExpenseType.MAINTENANCE) {
                        line.append(",\"")
                                .append(expense.getMaintenanceType() != null ? expense.getMaintenanceType() : "")
                                .append("\"");
                    } else if (expense.getExpenseType() == ExpenseType.TRANSPORTATION) {
                        line.append(",\"").append(expense.getFuelQuantity() != null ? expense.getFuelQuantity() : 0)
                                .append("\"");
                        line.append(",\"")
                                .append(expense.getVehicleDetails() != null ? expense.getVehicleDetails() : "")
                                .append("\"");
                    } else if (expense.getExpenseType() == ExpenseType.SPARES_PARTS) {
                        line.append(",\"").append(expense.getPartName() != null ? expense.getPartName() : "")
                                .append("\"");
                        line.append(",\"")
                                .append(expense.getReplacementDetails() != null ? expense.getReplacementDetails() : "")
                                .append("\"");
                    }

                    writer.println(line);
                }
            }
            return null;
        }

        model.addAttribute("activeTab", "expenses");
        model.addAttribute("expenses", expenses);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("expenseType", expenseType);

        return getViewPath("expenses/report");
    }

    @GetMapping("/delete/{id}")
    public String deleteExpense(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            var expenseOpt = expenseService.getExpenseById(id);
            if (expenseOpt.isPresent()) {
                ExpenseType type = expenseOpt.get().getExpenseType();
                expenseService.deleteExpense(id);
                redirectAttributes.addFlashAttribute("success", "Expense deleted successfully");
                // Check if expenseType is null before redirecting
                return type != null ? getRedirectUrl(type) : "redirect:/expenses";
            } else {
                redirectAttributes.addFlashAttribute("error", "Expense not found");
                return "redirect:/expenses";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting expense: " + e.getMessage());
            return "redirect:/expenses";
        }
    }

    private String getRedirectUrl(ExpenseType expenseType) {
        if (expenseType == null) {
            return "redirect:/expenses";
        }

        return switch (expenseType) {
            case ELECTRICITY_BILLS -> "redirect:/expenses/electricity";
            case LABOUR -> "redirect:/expenses/labour";
            case MAINTENANCE -> "redirect:/expenses/maintenance";
            case TRANSPORTATION -> "redirect:/expenses/transportation";
            case SPARES_PARTS -> "redirect:/expenses/spares";
            case MISCELLANEOUS -> "redirect:/expenses/miscellaneous";
        };
    }
}