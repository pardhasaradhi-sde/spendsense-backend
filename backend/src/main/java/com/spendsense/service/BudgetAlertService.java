package com.spendsense.service;

import com.spendsense.model.Budget;
import com.spendsense.model.Transaction;
import com.spendsense.model.User;
import com.spendsense.repository.BudgetRepository;
import com.spendsense.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled service to monitor budgets and send alerts
 * Runs daily at 8 AM to check budget usage and send notifications
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BudgetAlertService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final EmailService emailService;

    // Alert thresholds
    private static final int WARNING_THRESHOLD = 80; // 80% of budget → send alert
    private static final int CRITICAL_THRESHOLD = 95; // 95% of budget → log critical warning
    private static final int ALERT_COOLDOWN_HOURS = 24;

    /**
     * Check all budgets and send alerts where spending >= 80% of the monthly limit.
     * Called by BudgetAlertScheduler (daily at 8 AM) and can be called inline.
     */
    @Transactional
    public void checkBudgetsAndSendAlerts() {
        log.info("Starting budget alert check job");

        try {
            List<Budget> allBudgets = budgetRepository.findAllWithUser();
            log.info("Checking {} budgets for alerts", allBudgets.size());

            int alertsSent = 0;

            for (Budget budget : allBudgets) {
                try {
                    if (shouldSendAlert(budget)) {
                        sendBudgetAlert(budget);
                        alertsSent++;
                    }
                } catch (Exception e) {
                    log.error("Failed to process budget alert for budget {}: {}",
                            budget.getId(), e.getMessage(), e);
                }
            }

            log.info("Budget alert check completed. Alerts sent: {}", alertsSent);

        } catch (Exception e) {
            log.error("Error in budget alert check job", e);
        }
    }

    /**
     * Determine if an alert should be sent for this budget
     */
    private boolean shouldSendAlert(Budget budget) {
        if (budget.getLastAlertSent() != null) {
            LocalDateTime cooldownTime = budget.getLastAlertSent().plusHours(ALERT_COOLDOWN_HOURS);
            if (LocalDateTime.now().isBefore(cooldownTime)) {
                log.debug("Budget {} is in alert cooldown period", budget.getId());
                return false;
            }
        }

        BigDecimal totalSpent = getMonthlyExpenses(budget.getUser().getId());

        if (budget.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            return false;

        double percentUsed = totalSpent.divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();

        log.debug("Budget {} usage: {}%", budget.getId(), percentUsed);
        if (percentUsed >= CRITICAL_THRESHOLD) {
            log.warn("CRITICAL: Budget {} is {}% consumed (>={}%)", budget.getId(),
                    String.format("%.1f", percentUsed), CRITICAL_THRESHOLD);
        }
        return percentUsed >= WARNING_THRESHOLD;
    }

    /**
     * Send budget alert email
     */
    private void sendBudgetAlert(Budget budget) {
        User user = budget.getUser();

        BigDecimal totalSpent = getMonthlyExpenses(user.getId());
        BigDecimal remaining = budget.getAmount().subtract(totalSpent);
        double percentUsed = totalSpent.divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();

        try {
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                emailService.sendBudgetAlertEmail(
                        user.getEmail(),
                        user.getName() != null ? user.getName() : "User",
                        String.format("%.2f", budget.getAmount()),
                        String.format("%.2f", totalSpent),
                        String.format("%.2f", remaining),
                        String.format("%.1f", percentUsed));

                budget.setLastAlertSent(LocalDateTime.now());
                budgetRepository.save(budget);
                log.info("Budget alert email sent to user {} ({})", user.getId(), user.getEmail());
            } else {
                log.warn("User {} has no email address, skipping alert", user.getId());
            }
        } catch (Exception e) {
            log.error("Failed to send budget alert email to user {}: {}", user.getId(), e.getMessage(), e);
        }
    }

    /** Returns total EXPENSE-only spending for the current calendar month */
    private BigDecimal getMonthlyExpenses(java.util.UUID userId) {
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return transactionRepository
                .findByUserIdAndDateAfterOrderByDateDesc(userId, startOfMonth)
                .stream()
                .filter(t -> t.getType() == com.spendsense.model.enums.TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
