package com.inventory.service;

import com.inventory.model.Dealer;
import com.inventory.repository.DealerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DealerService {
    
    @Autowired
    private DealerRepository dealerRepository;
    
    public List<Dealer> getAllDealers() {
        return dealerRepository.findByDeletedFalse();
    }
    
    public Dealer getDealerById(Long id) {
        return dealerRepository.findByIdIncludeDeleted(id);
    }
    
    public Dealer saveDealer(Dealer dealer) {
        return dealerRepository.save(dealer);
    }
    
    public void deleteDealer(Long id) {
        Dealer dealer = dealerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Dealer not found"));
        dealer.setDeleted(true);
        dealerRepository.save(dealer);
    }
    
    public Dealer updateDealer(Long id, Dealer dealerDetails) {
        Dealer dealer = dealerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Dealer not found"));
        
        dealer.setName(dealerDetails.getName());
        dealer.setPhoneNumber(dealerDetails.getPhoneNumber());
        dealer.setAddress(dealerDetails.getAddress());
        dealer.setGstNumber(dealerDetails.getGstNumber());
        
        return dealerRepository.save(dealer);
    }
} 