package com.inventory.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import java.util.Map;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "company")
public class CompanyInfoProperties {
    private String name;
    private String address;
    private String gst;
    private String hsn;
    private String mobile;
    private String email;
    private double cgst;
    private double sgst;
    private double blockTax;
} 