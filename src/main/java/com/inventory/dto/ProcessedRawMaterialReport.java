package com.inventory.dto;

import com.inventory.model.ProcessedRawMaterial;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ProcessedRawMaterialReport {
    private LocalDate date; // For backward compatibility
    private LocalDate startDate;
    private LocalDate endDate;
    private List<ProcessedRawMaterial> processedEntries;
    private Double totalCost;

    // Calculated getters for summary data
    public int getTotalEntries() {
        return processedEntries != null ? processedEntries.size() : 0;
    }

    public double getAverageCost() {
        if (processedEntries == null || processedEntries.isEmpty()) {
            return 0.0;
        }
        return totalCost / processedEntries.size();
    }
}