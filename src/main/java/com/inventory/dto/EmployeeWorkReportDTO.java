package com.inventory.dto;

import com.inventory.model.LabourEntry;
import java.util.List;

public class EmployeeWorkReportDTO {
    private int totalDays;
    private double totalHours;
    private double totalSalary;
    private double averagePerDay;
    private double totalAdvanceAdjustment;
    private double totalNetPayable;
    private double openingAdvance;
    private double advancesGiven;
    private double closingAdvance;
    private List<LabourEntry> labourEntries;
    
    public EmployeeWorkReportDTO() {}
    
    public EmployeeWorkReportDTO(int totalDays, double totalHours, double totalSalary, 
                                double averagePerDay, List<LabourEntry> labourEntries) {
        this.totalDays = totalDays;
        this.totalHours = totalHours;
        this.totalSalary = totalSalary;
        this.averagePerDay = averagePerDay;
        this.labourEntries = labourEntries;
    }

    public EmployeeWorkReportDTO(int totalDays, double totalHours, double totalSalary, 
                                double averagePerDay, double totalAdvanceAdjustment, 
                                double totalNetPayable, double openingAdvance, 
                                double advancesGiven, double closingAdvance, 
                                List<LabourEntry> labourEntries) {
        this.totalDays = totalDays;
        this.totalHours = totalHours;
        this.totalSalary = totalSalary;
        this.averagePerDay = averagePerDay;
        this.totalAdvanceAdjustment = totalAdvanceAdjustment;
        this.totalNetPayable = totalNetPayable;
        this.openingAdvance = openingAdvance;
        this.advancesGiven = advancesGiven;
        this.closingAdvance = closingAdvance;
        this.labourEntries = labourEntries;
    }
    
    // Getters and Setters
    public int getTotalDays() {
        return totalDays;
    }
    
    public void setTotalDays(int totalDays) {
        this.totalDays = totalDays;
    }
    
    public double getTotalHours() {
        return totalHours;
    }
    
    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }
    
    public double getTotalSalary() {
        return totalSalary;
    }
    
    public void setTotalSalary(double totalSalary) {
        this.totalSalary = totalSalary;
    }
    
    public double getAveragePerDay() {
        return averagePerDay;
    }
    
    public void setAveragePerDay(double averagePerDay) {
        this.averagePerDay = averagePerDay;
    }
    
    public List<LabourEntry> getLabourEntries() {
        return labourEntries;
    }
    
    public void setLabourEntries(List<LabourEntry> labourEntries) {
        this.labourEntries = labourEntries;
    }

    public double getTotalAdvanceAdjustment() {
        return totalAdvanceAdjustment;
    }

    public void setTotalAdvanceAdjustment(double totalAdvanceAdjustment) {
        this.totalAdvanceAdjustment = totalAdvanceAdjustment;
    }

    public double getTotalNetPayable() {
        return totalNetPayable;
    }

    public void setTotalNetPayable(double totalNetPayable) {
        this.totalNetPayable = totalNetPayable;
    }

    public double getOpeningAdvance() {
        return openingAdvance;
    }

    public void setOpeningAdvance(double openingAdvance) {
        this.openingAdvance = openingAdvance;
    }

    public double getAdvancesGiven() {
        return advancesGiven;
    }

    public void setAdvancesGiven(double advancesGiven) {
        this.advancesGiven = advancesGiven;
    }

    public double getClosingAdvance() {
        return closingAdvance;
    }

    public void setClosingAdvance(double closingAdvance) {
        this.closingAdvance = closingAdvance;
    }
} 