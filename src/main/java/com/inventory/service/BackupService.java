package com.inventory.service;

import com.opencsv.CSVWriter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class BackupService {

    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    public Resource generateBackup() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);

            // List of entities to backup
            String[] tables = {
                "dealers", "fiber_stock", "parties", "pith_stock",
                "production", "raw_materials", "sales", "accounts"
            };

            for (String tableName : tables) {
                // Create CSV content for each table
                String csvContent = generateCsvForTable(tableName);
                
                // Add CSV to zip file
                ZipEntry entry = new ZipEntry(tableName + ".csv");
                zos.putNextEntry(entry);
                zos.write(csvContent.getBytes());
                zos.closeEntry();
            }

            zos.close();
            return new ByteArrayResource(baos.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate backup", e);
        }
    }

    private String generateCsvForTable(String tableName) {
        try {
            StringWriter writer = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(writer);

            // Get column names
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns " +
                "WHERE table_name = ? ORDER BY ordinal_position", tableName);
            
            String[] headers = columns.stream()
                .map(col -> col.get("column_name").toString())
                .toArray(String[]::new);
            
            csvWriter.writeNext(headers);

            // Get data
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM " + tableName);
            
            for (Map<String, Object> row : rows) {
                String[] data = new String[headers.length];
                for (int i = 0; i < headers.length; i++) {
                    Object value = row.get(headers[i]);
                    data[i] = value != null ? value.toString() : "";
                }
                csvWriter.writeNext(data);
            }

            csvWriter.close();
            return writer.toString();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV for table: " + tableName, e);
        }
    }
} 