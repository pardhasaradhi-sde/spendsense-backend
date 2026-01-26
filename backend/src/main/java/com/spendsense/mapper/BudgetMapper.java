package com.spendsense.mapper;

import com.spendsense.dto.request.CreateBudgetRequest;
import com.spendsense.dto.response.BudgetResponse;
import com.spendsense.model.Budget;
import org.springframework.stereotype.Component;

@Component
public class BudgetMapper {
    public Budget toEntity(CreateBudgetRequest request)
    {
        Budget budget=new Budget();
        budget.setAmount(request.getAmount());
        return budget;
    }

    public BudgetResponse toResponse(Budget budget)
    {
        return BudgetResponse.builder()
                .id(budget.getId())
                .amount(budget.getAmount())
                .lastAlertSent(budget.getLastAlertSent())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }
}
