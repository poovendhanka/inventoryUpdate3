package com.inventory.service;

import com.inventory.model.Dealer;
import com.inventory.repository.DealerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DealerService {
    private final DealerRepository dealerRepository;
    
    public List<Dealer> getAllDealers() {
        // Check if this method is adding default dealers
        return dealerRepository.findAll();
    }
    
    public Dealer saveDealer(Dealer dealer) {
        return dealerRepository.save(dealer);
    }
    
    public void deleteDealer(Long id) {
        dealerRepository.deleteById(id);
    }
    
    public Dealer getDealerById(Long id) {
        return dealerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Dealer not found"));
    }
} 