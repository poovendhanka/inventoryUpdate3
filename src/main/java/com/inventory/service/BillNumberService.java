package com.inventory.service;

import com.inventory.model.BillNumber;
import com.inventory.repository.BillNumberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BillNumberService {
    private final BillNumberRepository billNumberRepository;

    @Transactional
    public synchronized String getNextBillNumber() {
        BillNumber billNumber = billNumberRepository.findAll().stream().findFirst().orElse(null);
        if (billNumber == null) {
            billNumber = new BillNumber();
            billNumber.setLastNumber(10000);
        } else {
            billNumber.setLastNumber(billNumber.getLastNumber() + 1);
        }
        billNumberRepository.save(billNumber);
        return String.format("%05d", billNumber.getLastNumber());
    }
} 