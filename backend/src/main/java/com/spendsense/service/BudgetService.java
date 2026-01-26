package com.spendsense.service;

import com.spendsense.dto.request.CreateBudgetRequest;
import com.spendsense.dto.request.UpdateBudgetRequest;
import com.spendsense.dto.response.BudgetResponse;
import com.spendsense.exception.BadRequestException;
import com.spendsense.exception.ResourceNotFoundException;
import com.spendsense.mapper.BudgetMapper;
import com.spendsense.model.Budget;
import com.spendsense.model.User;
import com.spendsense.repository.BudgetRepository;
import com.spendsense.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final BudgetMapper budgetMapper;

    public BudgetResponse createBudget(UUID userId, CreateBudgetRequest request)
    {
        if(budgetRepository.existsByUserId(userId)){
            throw new BadRequestException("User Already has a Budget.Use Update instead");
        }
        User user=userRepository.findById(userId)
                .orElseThrow(()->new ResourceNotFoundException("User Not Found"));
        Budget budget=budgetMapper.toEntity(request);
        budget.setUser(user);
        budgetRepository.save(budget);
        return budgetMapper.toResponse(budget);
    }

    public BudgetResponse getUserBudget(UUID userId){
        Budget budget=budgetRepository.findByUserId(userId)
                .orElseThrow(()->new ResourceNotFoundException("Budget Not Found"));
        return budgetMapper.toResponse(budget);
    }
    public BudgetResponse updateBudget(UUID userId, UpdateBudgetRequest request)
    {
        Budget budget=budgetRepository.findByUserId(userId)
                .orElseThrow(()->new ResourceNotFoundException("Budget Not Found"));
        if(request.getAmount()!=null){
            budget.setAmount(request.getAmount());
        }
        Budget updated=budgetRepository.save(budget);
        return budgetMapper.toResponse(updated);
    }

    public void deleteBudget(UUID userId){
        Budget budget=budgetRepository.findByUserId(userId)
                .orElseThrow(()->new ResourceNotFoundException("Budget Not Found"));
        budgetRepository.delete(budget);
    }
}
