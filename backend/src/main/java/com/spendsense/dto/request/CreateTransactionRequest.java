package com.spendsense.dto.request;

import com.spendsense.model.enums.RecurringInterval;
import com.spendsense.model.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateTransactionRequest {
    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @NotNull(message = "Amount is Required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Date is required")
    private LocalDateTime date;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    @NotNull(message = "AccountId is required")
    private UUID accountId;

    @Size(max = 500, message = "Receipt URL cannot exceed 500 characters")
    private String receiptUrl;

    private Boolean isRecurring = false;

    private RecurringInterval recurringInterval;
}
