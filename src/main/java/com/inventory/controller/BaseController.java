package com.inventory.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public abstract class BaseController {
    
    protected boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }
    
    protected String getViewPath(String viewName) {
        return viewName;
    }
    
    protected ResponseEntity<?> ajaxRedirect(String url) {
        return ResponseEntity.ok(Map.of("redirect", url));
    }
    
    protected ResponseEntity<?> ajaxError(String message) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", message));
    }
    
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        // Add any common attributes needed across all pages
    }
} 