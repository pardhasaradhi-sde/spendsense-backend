package com.spendsense.dto.request;

import com.spendsense.model.enums.RecurringInterval;
import com.spendsense.model.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateTransactionRequest {

    private TransactionType type;

    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private LocalDateTime date;

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    @Size(max = 500, message = "Receipt URL cannot exceed 500 characters")
    private String receiptUrl;

    private Boolean isRecurring;

    private RecurringInterval recurringInterval;
}
