package com.inventory.repository;

import com.inventory.model.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DealerRepository extends JpaRepository<Dealer, Long> {
} 