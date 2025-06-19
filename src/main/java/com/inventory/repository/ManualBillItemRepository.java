package com.inventory.repository;

import com.inventory.model.ManualBillItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManualBillItemRepository extends JpaRepository<ManualBillItem, Long> {
    
    List<ManualBillItem> findByManualBillId(Long manualBillId);
    
    void deleteByManualBillId(Long manualBillId);
} 