package com.spendsense.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {
    private UUID id;
    private BigDecimal amount;
    private LocalDateTime lastAlertSent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
