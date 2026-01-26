package com.spendsense.repository;

import com.spendsense.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    //find budget based on user
    Optional<Budget> findByUserId(UUID userId);

    //check if budget exits
    boolean existsByUserId(UUID userId);

    //delete budget based on user
    void  deleteByUserId(UUID userId);

}
