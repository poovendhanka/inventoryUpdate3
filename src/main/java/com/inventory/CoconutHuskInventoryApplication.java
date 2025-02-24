package com.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class CoconutHuskInventoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoconutHuskInventoryApplication.class, args);
    }
} 