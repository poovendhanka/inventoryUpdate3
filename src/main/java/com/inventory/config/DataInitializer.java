package com.inventory.config;

import com.inventory.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserService userService;
    
    @Override
    public void run(String... args) throws Exception {
        try {
            userService.initializeDefaultAdmin();
            log.info("Default admin user initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing default admin user", e);
        }
    }
} 