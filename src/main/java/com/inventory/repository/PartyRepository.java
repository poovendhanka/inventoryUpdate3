package com.inventory.repository;

import com.inventory.model.Party;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyRepository extends JpaRepository<Party, Long> {
} 