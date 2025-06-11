package com.inventory.repository;

import com.inventory.model.BillNumber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillNumberRepository extends JpaRepository<BillNumber, Long> {
} 