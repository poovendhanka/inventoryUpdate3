package com.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseBackupService {

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    public Resource createSqlBackup() throws IOException, InterruptedException {
        return createBackup("sql", "-f", "-");
    }

    public Resource createDumpBackup() throws IOException, InterruptedException {
        return createBackup("dump", "-Fc", "-f", "-");
    }

    private Resource createBackup(String format, String... additionalArgs) throws IOException, InterruptedException {
        // Extract database details from JDBC URL
        String host = extractHostFromUrl(databaseUrl);
        String port = extractPortFromUrl(databaseUrl);
        String databaseName = extractDatabaseNameFromUrl(databaseUrl);

        // Build pg_dump command
        List<String> command = new ArrayList<>();
        command.add("pg_dump");
        command.add("-h");
        command.add(host);
        command.add("-p");
        command.add(port);
        command.add("-U");
        command.add(databaseUsername);
        command.add("-d");
        command.add(databaseName);
        
        // Add format-specific arguments
        for (String arg : additionalArgs) {
            command.add(arg);
        }

        log.info("Starting {} database backup", format.toUpperCase());
        log.info("Command: {}", String.join(" ", command));

        // Execute pg_dump command
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.environment().put("PGPASSWORD", databasePassword);
        processBuilder.redirectErrorStream(false);

        Process process = processBuilder.start();

        // Read the backup data from stdout
        ByteArrayOutputStream backupData = new ByteArrayOutputStream();
        
        // Read stdout (backup data)
        try (InputStream inputStream = process.getInputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                backupData.write(buffer, 0, bytesRead);
            }
        }

        // Read stderr for error messages
        StringBuilder errorOutput = new StringBuilder();
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
                log.info("pg_dump stderr: {}", line);
            }
        }

        // Wait for process to complete
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            byte[] backupBytes = backupData.toByteArray();
            log.info("{} backup completed successfully. Size: {} bytes", format.toUpperCase(), backupBytes.length);
            return new ByteArrayResource(backupBytes);
        } else {
            log.error("{} backup failed with exit code: {}", format.toUpperCase(), exitCode);
            log.error("Error output: {}", errorOutput.toString());
            throw new IOException(format.toUpperCase() + " backup failed: " + errorOutput.toString());
        }
    }

    public String generateBackupFileName(String format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("husk_inventory_backup_%s.%s", timestamp, format);
    }

    private String extractHostFromUrl(String url) {
        // Extract host from jdbc:postgresql://host:port/database
        String[] parts = url.split("//")[1].split("/")[0].split(":");
        return parts[0];
    }

    private String extractPortFromUrl(String url) {
        // Extract port from jdbc:postgresql://host:port/database
        String[] parts = url.split("//")[1].split("/")[0].split(":");
        return parts.length > 1 ? parts[1] : "5432";
    }

    private String extractDatabaseNameFromUrl(String url) {
        // Extract database name from jdbc:postgresql://host:port/database
        String[] parts = url.split("/");
        return parts[parts.length - 1].split("\\?")[0];
    }
} 