package com.spendsense.repository;

import com.spendsense.model.Transaction;
import com.spendsense.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // ── Paginated finders — @EntityGraph eliminates N+1 on account ────────────

    /**
     * Paginated transactions for a user.
     * JOIN FETCH account in one query instead of N separate SELECTs.
     */
    @EntityGraph(attributePaths = { "account" })
    Page<Transaction> findByUserId(UUID userId, Pageable pageable);

    /**
     * Paginated transactions for a specific account owned by user.
     * JOIN FETCH account eliminates the N+1 when mapper reads account.getName().
     */
    @EntityGraph(attributePaths = { "account" })
    Page<Transaction> findByAccountIdAndUserId(UUID accountId, UUID userId, Pageable pageable);

    // ── Single-row finders ────────────────────────────────────────────────────

    /** Used by AccountController — account already in hand, no extra join needed */
    Page<Transaction> findByAccountId(UUID accountId, Pageable pageable);

    /** Used by TransactionService.getTransaction() / update / delete */
    @EntityGraph(attributePaths = { "account" })
    Optional<Transaction> findByIdAndUserId(UUID transactionId, UUID userId);

    // ── List finders (export, analytics, AI) ─────────────────────────────────

    /**
     * All transactions for a user (CSV/PDF export).
     * JOIN FETCH account — ExportService calls transaction.getAccount().getName().
     */
    @EntityGraph(attributePaths = { "account" })
    List<Transaction> findByUserId(UUID userId);

    /** Filtered by type — no account access downstream, no JOIN FETCH needed */
    List<Transaction> findByUserIdAndType(UUID userId, TransactionType type);

    /**
     * Date-range list (analytics comparison, export with filter).
     * JOIN FETCH account — ExportService iterates and accesses account name.
     */
    @EntityGraph(attributePaths = { "account" })
    List<Transaction> findByUserIdAndDateBetween(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Date-range ordered — used for analytics spending comparison.
     * JOIN FETCH account for export path; analytics doesn't access account, but
     * the JOIN FETCH is inexpensive and future-proofs the query.
     */
    @EntityGraph(attributePaths = { "account" })
    List<Transaction> findByUserIdAndDateBetweenOrderByDateDesc(UUID userId, LocalDateTime startDate,
            LocalDateTime endDate);

    /**
     * After-date ordered — used by AiInsightsService, BudgetAlertService,
     * AnalyticsService.
     * JOIN FETCH account prevents N+1 in any downstream account access.
     */
    @EntityGraph(attributePaths = { "account" })
    List<Transaction> findByUserIdAndDateAfterOrderByDateDesc(UUID userId, LocalDateTime date);

    // ── Scheduled job finder — requires user + account in one query ───────────

    /**
     * Finds recurring templates that are due.
     * Uses JPQL JOIN FETCH to load both user and account associations eagerly,
     * preventing 2×N extra SELECTs inside RecurringTransactionProcessor.
     */
    @Query("""
            SELECT t FROM Transaction t
            JOIN FETCH t.user
            JOIN FETCH t.account
            WHERE t.isRecurring = true
            AND t.nextRecurringDate < :now
            """)
    List<Transaction> findByIsRecurringTrueAndNextRecurringDateBefore(@Param("now") LocalDateTime now);

    // ── Aggregate query ───────────────────────────────────────────────────────

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type")
    BigDecimal sumAmountByUserIdAndType(@Param("userId") UUID userId,
            @Param("type") TransactionType type);
}
