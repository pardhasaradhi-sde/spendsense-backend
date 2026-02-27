package com.spendsense.scheduler;

import com.spendsense.model.Account;
import com.spendsense.model.Transaction;
import com.spendsense.model.enums.RecurringInterval;
import com.spendsense.model.enums.TransactionStatus;
import com.spendsense.model.enums.TransactionType;
import com.spendsense.repository.AccountRepository;
import com.spendsense.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job that processes due recurring transactions.
 * Runs daily at 2 AM — finds all recurring templates overdue and
 * creates a new transaction instance + advances the next-run date.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RecurringTransactionScheduler {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Scheduled(cron = "${scheduling.recurring-transactions.cron}")
    @Transactional
    public void processRecurringTransactions() {
        log.info("Starting recurring transaction processing job");

        LocalDateTime now = LocalDateTime.now();
        List<Transaction> dueTransactions = transactionRepository.findByIsRecurringTrueAndNextRecurringDateBefore(now);

        log.info("Found {} recurring transactions to process", dueTransactions.size());

        int successCount = 0, failureCount = 0;

        for (Transaction template : dueTransactions) {
            try {
                processRecurringTransaction(template);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to process recurring transaction {}: {}",
                        template.getId(), e.getMessage(), e);
                failureCount++;
            }
        }

        log.info("Recurring transaction processing complete. Success: {}, Failed: {}",
                successCount, failureCount);
    }

    private void processRecurringTransaction(Transaction template) {
        Transaction newTx = new Transaction();
        newTx.setType(template.getType());
        newTx.setAmount(template.getAmount());
        newTx.setDescription(template.getDescription());
        newTx.setDate(LocalDateTime.now());
        newTx.setCategory(template.getCategory());
        newTx.setUser(template.getUser());
        newTx.setAccount(template.getAccount());
        newTx.setStatus(TransactionStatus.COMPLETED);
        newTx.setIsRecurring(false);
        newTx.setRecurringInterval(null);
        newTx.setNextRecurringDate(null);

        updateAccountBalance(template.getAccount(), newTx);
        transactionRepository.save(newTx);

        LocalDateTime nextDate = calculateNextDate(template.getNextRecurringDate(),
                template.getRecurringInterval());
        template.setNextRecurringDate(nextDate);
        template.setLastProcessed(LocalDateTime.now());
        transactionRepository.save(template);

        log.debug("Recurring transaction processed. Next occurrence: {}", nextDate);
    }

    private void updateAccountBalance(Account account, Transaction tx) {
        BigDecimal current = account.getBalance();
        BigDecimal updated = tx.getType() == TransactionType.INCOME
                ? current.add(tx.getAmount())
                : current.subtract(tx.getAmount());
        account.setBalance(updated);
        accountRepository.save(account);
    }

    private LocalDateTime calculateNextDate(LocalDateTime current, RecurringInterval interval) {
        return switch (interval) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
            case YEARLY -> current.plusYears(1);
        };
    }
}
