package com.spendsense.dto.response;

import com.spendsense.model.enums.RecurringInterval;
import com.spendsense.model.enums.TransactionStatus;
import com.spendsense.model.enums.TransactionType;
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
public class TransactionResponse {
    private UUID id;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private LocalDateTime date;
    private String category;
    private String receiptUrl;
    private Boolean isRecurring;
    private RecurringInterval recurringInterval;
    private TransactionStatus status;
    private UUID accountId;
    private String accountName;
    private LocalDateTime createdAt;
}
