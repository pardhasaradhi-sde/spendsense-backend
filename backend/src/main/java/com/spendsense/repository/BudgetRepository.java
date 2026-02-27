package com.spendsense.repository;

import com.spendsense.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    // find budget by user
    Optional<Budget> findByUserId(UUID userId);

    // find all budgets for a user
    List<Budget> findAllByUserId(UUID userId);

    // check if budget exists
    boolean existsByUserId(UUID userId);

    // delete budget by user
    void deleteByUserId(UUID userId);

    /**
     * Used by BudgetAlertService — JOIN FETCH user to avoid N+1.
     * Without this, accessing budget.getUser().getId() fires one SELECT per budget.
     */
    @Query("SELECT b FROM Budget b JOIN FETCH b.user")
    List<Budget> findAllWithUser();
}
