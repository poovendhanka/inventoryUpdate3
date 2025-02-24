package com.inventory.dto;

import com.inventory.model.ProcessedRawMaterial;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ProcessedRawMaterialReport {
    private LocalDate date;
    private List<ProcessedRawMaterial> processedEntries;
} 