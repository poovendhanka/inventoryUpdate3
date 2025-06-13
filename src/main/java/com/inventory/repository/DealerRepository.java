package com.inventory.repository;

import com.inventory.model.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface DealerRepository extends JpaRepository<Dealer, Long> {
    List<Dealer> findByDeletedFalse();
    
    @Query("SELECT d FROM Dealer d WHERE d.deleted = false OR d.id = ?1")
    Dealer findByIdIncludeDeleted(Long id);
} 