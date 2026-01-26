package com.spendsense.repository;

import com.spendsense.model.Transaction;
import com.spendsense.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    //find all transactions for a user(with pagination)
    Page<Transaction> findByUserId(UUID userId, Pageable pageable);

    //find all transactoon for an account(with pagination)
    Page<Transaction> findByAccountId(UUID accountId, Pageable pageable);
    //find transaction on user and tid
    Optional<Transaction> findByIdAndUserId(UUID transactionId, UUID userId);
    //find transactions based on account and user
    Page<Transaction> findByAccountIdAndUserId(UUID accountId, UUID userId,Pageable pageable);

    //find transactions by type
    List<Transaction> findByUserIdAndType(UUID userId, TransactionType type);

    //find transactions in date range
    List<Transaction> findByUserIdAndDateBetween(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    //find recurring transactions that need processing
    List<Transaction> findByIsRecurringTrueAndNextRecurringDateBefore(LocalDateTime date);

    //calculate total for user by type
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type")
    BigDecimal sumAmountByUserIdAndType(@Param("userId") UUID userId,
                                        @Param("type") TransactionType type);
}
