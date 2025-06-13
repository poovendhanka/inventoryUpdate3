package com.inventory.service;

import com.inventory.model.Party;
import com.inventory.repository.PartyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PartyService {
    
    @Autowired
    private PartyRepository partyRepository;
    
    public List<Party> getAllParties() {
        return partyRepository.findByDeletedFalse();
    }
    
    public Party getPartyById(Long id) {
        return partyRepository.findByIdIncludeDeleted(id);
    }
    
    public Party saveParty(Party party) {
        return partyRepository.save(party);
    }
    
    public void deleteParty(Long id) {
        Party party = partyRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Party not found"));
        party.setDeleted(true);
        partyRepository.save(party);
    }
    
    public Party updateParty(Long id, Party partyDetails) {
        Party party = partyRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Party not found"));
        
        party.setName(partyDetails.getName());
        party.setSinNumber(partyDetails.getSinNumber());
        party.setPhoneNumber(partyDetails.getPhoneNumber());
        party.setAddress(partyDetails.getAddress());
        
        return partyRepository.save(party);
    }
} 