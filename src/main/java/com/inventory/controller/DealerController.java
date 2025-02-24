package com.inventory.controller;

import com.inventory.model.Dealer;
import com.inventory.service.DealerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/dealers")
@RequiredArgsConstructor
public class DealerController extends BaseController {
    
    private final DealerService dealerService;
    
    @GetMapping("/{id}")
    @ResponseBody
    public Dealer getDealer(@PathVariable Long id) {
        return dealerService.getDealerById(id);
    }
} 