package com.inventory.controller;

import com.inventory.service.BackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/backup")
@RequiredArgsConstructor
public class BackupController extends BaseController {
    
    private final BackupService backupService;
    
    @GetMapping
    public String showBackupPage(Model model) {
        model.addAttribute("activeTab", "backup");
        return getViewPath("backup/index");
    }
    
    @GetMapping("/generate")
    public ResponseEntity<Resource> generateBackup() {
        Resource zipFile = backupService.generateBackup();
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "backup_" + timestamp + ".zip";
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(zipFile);
    }
} 