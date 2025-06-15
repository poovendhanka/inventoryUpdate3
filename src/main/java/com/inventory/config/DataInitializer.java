package com.inventory.config;

import com.inventory.model.User;
import com.inventory.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserService userService;
    
    @Autowired
    public DataInitializer(@Lazy UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public void run(String... args) throws Exception {
        initializeDefaultUsers();
    }
    
    private void initializeDefaultUsers() {
        // Create default admin user if not exists
        if (!userService.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123"); // This will be encoded by the service
            admin.setFullName("System Administrator");
            admin.setRole(User.Role.ADMIN);
            admin.setEnabled(true);
            
            try {
                userService.createUser(admin);
                log.info("Default admin user created successfully");
                log.info("Username: admin, Password: admin123");
            } catch (Exception e) {
                log.error("Error creating default admin user: {}", e.getMessage());
            }
        }
        
        // Create default regular user if not exists
        if (!userService.existsByUsername("user")) {
            User user = new User();
            user.setUsername("user");
            user.setPassword("user123"); // This will be encoded by the service
            user.setFullName("Regular User");
            user.setRole(User.Role.USER);
            user.setEnabled(true);
            
            try {
                userService.createUser(user);
                log.info("Default regular user created successfully");
                log.info("Username: user, Password: user123");
            } catch (Exception e) {
                log.error("Error creating default regular user: {}", e.getMessage());
            }
        }
    }
} 