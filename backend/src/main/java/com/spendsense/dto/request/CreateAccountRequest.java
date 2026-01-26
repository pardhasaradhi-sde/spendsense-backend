package com.spendsense.dto.request;

import com.spendsense.model.enums.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAccountRequest {

    @NotBlank(message = "Account name is required")
    @Size(max = 255, message = "Account name cannot exceed 255 characters")
    private String name;

    @NotNull(message = "Account type is required")
    private AccountType type;

    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.0", message = "Balance cannot be negative")
    private BigDecimal balance;

    private Boolean isDefault=false;
}
