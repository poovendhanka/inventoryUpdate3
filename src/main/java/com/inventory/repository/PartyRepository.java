package com.inventory.repository;

import com.inventory.model.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PartyRepository extends JpaRepository<Party, Long> {
    List<Party> findByDeletedFalse();
    
    @Query("SELECT p FROM Party p WHERE p.deleted = false OR p.id = ?1")
    Party findByIdIncludeDeleted(Long id);
} 