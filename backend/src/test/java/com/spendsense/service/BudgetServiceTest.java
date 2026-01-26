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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BudgetMapper budgetMapper;

    @InjectMocks
    private BudgetService budgetService;

    private User testUser;
    private Budget testBudget;
    private BudgetResponse budgetResponse;
    private CreateBudgetRequest createRequest;
    private UpdateBudgetRequest updateRequest;
    private UUID userId;
    private UUID budgetId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        budgetId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");

        testBudget = new Budget();
        testBudget.setId(budgetId);
        testBudget.setUser(testUser);
        testBudget.setAmount(BigDecimal.valueOf(5000.00));

        budgetResponse = new BudgetResponse();
        budgetResponse.setId(budgetId);
        budgetResponse.setAmount(BigDecimal.valueOf(5000.00));

        createRequest = new CreateBudgetRequest();
        createRequest.setAmount(BigDecimal.valueOf(5000.00));

        updateRequest = new UpdateBudgetRequest();
        updateRequest.setAmount(BigDecimal.valueOf(6000.00));
    }

    @Test
    void createBudget_WhenUserDoesNotHaveBudget_ShouldCreateBudget() {
        // Arrange
        when(budgetRepository.existsByUserId(userId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(budgetMapper.toEntity(createRequest)).thenReturn(testBudget);
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);
        when(budgetMapper.toResponse(testBudget)).thenReturn(budgetResponse);

        // Act
        BudgetResponse result = budgetService.createBudget(userId, createRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000.00));
        verify(budgetRepository).existsByUserId(userId);
        verify(userRepository).findById(userId);
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void createBudget_WhenUserAlreadyHasBudget_ShouldThrowException() {
        // Arrange
        when(budgetRepository.existsByUserId(userId)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> budgetService.createBudget(userId, createRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("User Already has a Budget.Use Update instead");

        verify(budgetRepository).existsByUserId(userId);
        verify(userRepository, never()).findById(any());
        verify(budgetRepository, never()).save(any());
    }

    @Test
    void createBudget_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(budgetRepository.existsByUserId(userId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> budgetService.createBudget(userId, createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User Not Found");

        verify(budgetRepository).existsByUserId(userId);
        verify(userRepository).findById(userId);
        verify(budgetRepository, never()).save(any());
    }

    @Test
    void getUserBudget_WhenBudgetExists_ShouldReturnBudget() {
        // Arrange
        when(budgetRepository.findByUserId(userId)).thenReturn(Optional.of(testBudget));
        when(budgetMapper.toResponse(testBudget)).thenReturn(budgetResponse);

        // Act
        BudgetResponse result = budgetService.getUserBudget(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(budgetId);
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000.00));
        verify(budgetRepository).findByUserId(userId);
        verify(budgetMapper).toResponse(testBudget);
    }

    @Test
    void getUserBudget_WhenBudgetNotFound_ShouldThrowException() {
        // Arrange
        when(budgetRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> budgetService.getUserBudget(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Budget Not Found");

        verify(budgetRepository).findByUserId(userId);
        verify(budgetMapper, never()).toResponse(any());
    }

    @Test
    void updateBudget_WhenBudgetExists_ShouldUpdateBudget() {
        // Arrange
        when(budgetRepository.findByUserId(userId)).thenReturn(Optional.of(testBudget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);
        when(budgetMapper.toResponse(testBudget)).thenReturn(budgetResponse);

        // Act
        BudgetResponse result = budgetService.updateBudget(userId, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(budgetRepository).findByUserId(userId);
        verify(budgetRepository).save(testBudget);
        verify(budgetMapper).toResponse(testBudget);
    }

    @Test
    void updateBudget_WhenBudgetNotFound_ShouldThrowException() {
        // Arrange
        when(budgetRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> budgetService.updateBudget(userId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Budget Not Found");

        verify(budgetRepository).findByUserId(userId);
        verify(budgetRepository, never()).save(any());
    }

    @Test
    void updateBudget_WhenAmountIsNull_ShouldNotUpdateAmount() {
        // Arrange
        updateRequest.setAmount(null);
        when(budgetRepository.findByUserId(userId)).thenReturn(Optional.of(testBudget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);
        when(budgetMapper.toResponse(testBudget)).thenReturn(budgetResponse);

        // Act
        BudgetResponse result = budgetService.updateBudget(userId, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(testBudget.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000.00)); // Unchanged
        verify(budgetRepository).save(testBudget);
    }

    @Test
    void deleteBudget_WhenBudgetExists_ShouldDeleteBudget() {
        // Arrange
        when(budgetRepository.findByUserId(userId)).thenReturn(Optional.of(testBudget));

        // Act
        budgetService.deleteBudget(userId);

        // Assert
        verify(budgetRepository).findByUserId(userId);
        verify(budgetRepository).delete(testBudget);
    }

    @Test
    void deleteBudget_WhenBudgetNotFound_ShouldThrowException() {
        // Arrange
        when(budgetRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> budgetService.deleteBudget(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Budget Not Found");

        verify(budgetRepository).findByUserId(userId);
        verify(budgetRepository, never()).delete(any());
    }
}
