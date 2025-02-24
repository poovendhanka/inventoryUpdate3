package com.inventory.service;

import com.inventory.model.Party;
import com.inventory.repository.PartyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartyService {
    private final PartyRepository partyRepository;
    
    public List<Party> getAllParties() {
        // Check if this method is adding default parties
        return partyRepository.findAll();
    }
    
    public Party saveParty(Party party) {
        return partyRepository.save(party);
    }
    
    public void deleteParty(Long id) {
        partyRepository.deleteById(id);
    }
    
    public Party getPartyById(Long id) {
        return partyRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Party not found"));
    }
} 