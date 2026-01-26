package com.spendsense.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateBudgetRequest {
    
    @DecimalMin(value = "0.01", message = "Budget amount must be greater than 0")
    private BigDecimal amount;
}
