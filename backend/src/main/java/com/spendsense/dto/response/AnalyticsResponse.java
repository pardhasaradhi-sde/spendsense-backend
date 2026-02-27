package com.spendsense.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netSavings;
    private Map<String, BigDecimal> categoryBreakdown;
    private Map<String, BigDecimal> monthlyTrends;
    private List<Map<String, Object>> topSpendingCategories;
    private BigDecimal averageMonthlyExpense;
    private Integer transactionCount;
    private Double savingsRate;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
}
