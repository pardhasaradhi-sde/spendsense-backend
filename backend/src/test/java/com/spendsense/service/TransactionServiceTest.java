package com.spendsense.service;

import com.spendsense.dto.request.CreateTransactionRequest;
import com.spendsense.dto.request.UpdateTransactionRequest;
import com.spendsense.dto.response.TransactionResponse;
import com.spendsense.exception.InvalidRecurringTransactionException;
import com.spendsense.exception.ResourceNotFoundException;
import com.spendsense.mapper.TransactionMapper;
import com.spendsense.model.Account;
import com.spendsense.model.Transaction;
import com.spendsense.model.User;
import com.spendsense.model.enums.RecurringInterval;
import com.spendsense.model.enums.TransactionType;
import com.spendsense.repository.AccountRepository;
import com.spendsense.repository.TransactionRepository;
import com.spendsense.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Account testAccount;
    private Transaction testTransaction;
    private TransactionResponse transactionResponse;
    private CreateTransactionRequest createRequest;
    private UpdateTransactionRequest updateRequest;
    private UUID userId;
    private UUID accountId;
    private UUID transactionId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        transactionId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");

        testAccount = new Account();
        testAccount.setId(accountId);
        testAccount.setUser(testUser);
        testAccount.setName("Checking");
        testAccount.setBalance(BigDecimal.valueOf(1000.00));

        testTransaction = new Transaction();
        testTransaction.setId(transactionId);
        testTransaction.setUser(testUser);
        testTransaction.setAccount(testAccount);
        testTransaction.setType(TransactionType.INCOME);
        testTransaction.setAmount(BigDecimal.valueOf(500.00));
        testTransaction.setDescription("Salary");
        testTransaction.setDate(LocalDateTime.now());

        transactionResponse = new TransactionResponse();
        transactionResponse.setId(transactionId);
        transactionResponse.setType(TransactionType.INCOME);
        transactionResponse.setAmount(BigDecimal.valueOf(500.00));
        transactionResponse.setDescription("Salary");

        createRequest = new CreateTransactionRequest();
        createRequest.setAccountId(accountId);
        createRequest.setType(TransactionType.INCOME);
        createRequest.setAmount(BigDecimal.valueOf(500.00));
        createRequest.setDescription("Salary");
        createRequest.setDate(LocalDateTime.now());

        updateRequest = new UpdateTransactionRequest();
        updateRequest.setType(TransactionType.EXPENSE);
        updateRequest.setAmount(BigDecimal.valueOf(300.00));
        updateRequest.setDescription("Groceries");
    }

    @Test
    void createTransaction_WhenValidRequest_ShouldCreateTransaction() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(testAccount));
        when(transactionMapper.toEntity(createRequest)).thenReturn(testTransaction);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(transactionMapper.toResponse(testTransaction)).thenReturn(transactionResponse);

        // Act
        TransactionResponse result = transactionService.createTransaction(userId, createRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
        verify(accountRepository).save(testAccount); // Balance update
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> transactionService.createTransaction(userId, createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createTransaction_WhenAccountNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> transactionService.createTransaction(userId, createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account not found");
    }

    @Test
    void createTransaction_WhenRecurringWithoutDate_ShouldThrowException() {
        // Arrange
        createRequest.setIsRecurring(true);
        createRequest.setDate(null);
        createRequest.setRecurringInterval(RecurringInterval.MONTHLY);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(testAccount));
        when(transactionMapper.toEntity(createRequest)).thenReturn(testTransaction);

        // Act & Assert
        assertThatThrownBy(() -> transactionService.createTransaction(userId, createRequest))
                .isInstanceOf(InvalidRecurringTransactionException.class)
                .hasMessageContaining("Recurring transaction requires date and recurringInterval");
    }

    @Test
    void createTransaction_WhenRecurringWithoutInterval_ShouldThrowException() {
        // Arrange
        createRequest.setIsRecurring(true);
        createRequest.setDate(LocalDateTime.now());
        createRequest.setRecurringInterval(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(testAccount));
        when(transactionMapper.toEntity(createRequest)).thenReturn(testTransaction);

        // Act & Assert
        assertThatThrownBy(() -> transactionService.createTransaction(userId, createRequest))
                .isInstanceOf(InvalidRecurringTransactionException.class)
                .hasMessageContaining("Recurring transaction requires date and recurringInterval");
    }

    @Test
    void getUserTransactions_ShouldReturnPagedTransactions() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Transaction> transactions = Arrays.asList(testTransaction);
        Page<Transaction> transactionPage = new PageImpl<>(transactions, pageable, 1);

        when(transactionRepository.findByUserId(userId, pageable)).thenReturn(transactionPage);
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(transactionResponse);

        // Act
        Page<TransactionResponse> result = transactionService.getUserTransactions(userId, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findByUserId(userId, pageable);
    }

    @Test
    void getAccountTransactions_WhenAccountExists_ShouldReturnPagedTransactions() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Transaction> transactions = Arrays.asList(testTransaction);
        Page<Transaction> transactionPage = new PageImpl<>(transactions, pageable, 1);

        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.findByAccountIdAndUserId(accountId, userId, pageable))
                .thenReturn(transactionPage);
        when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(transactionResponse);

        // Act
        Page<TransactionResponse> result = transactionService.getAccountTransactions(userId, accountId, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(accountRepository).findByIdAndUserId(accountId, userId);
    }

    @Test
    void getAccountTransactions_WhenAccountNotFound_ShouldThrowException() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> transactionService.getAccountTransactions(userId, accountId, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account not found");
    }

    @Test
    void getTransaction_WhenTransactionExists_ShouldReturnTransaction() {
        // Arrange
        when(transactionRepository.findByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.of(testTransaction));
        when(transactionMapper.toResponse(testTransaction)).thenReturn(transactionResponse);

        // Act
        TransactionResponse result = transactionService.getTransaction(userId, transactionId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(transactionId);
        verify(transactionRepository).findByIdAndUserId(transactionId, userId);
    }

    @Test
    void getTransaction_WhenTransactionNotFound_ShouldThrowException() {
        // Arrange
        when(transactionRepository.findByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> transactionService.getTransaction(userId, transactionId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found");
    }

    @Test
    void updateTransaction_WhenValidRequest_ShouldUpdateTransaction() {
        // Arrange
        when(transactionRepository.findByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(transactionMapper.toResponse(testTransaction)).thenReturn(transactionResponse);

        // Act
        TransactionResponse result = transactionService.updateTransaction(userId, transactionId, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(transactionRepository).findByIdAndUserId(transactionId, userId);
        verify(accountRepository, times(2)).save(testAccount); // Revert and update
        verify(transactionRepository).save(testTransaction);
    }

    @Test
    void updateTransaction_WhenTransactionNotFound_ShouldThrowException() {
        // Arrange
        when(transactionRepository.findByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> transactionService.updateTransaction(userId, transactionId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found");
    }

    @Test
    void deleteTransaction_WhenTransactionExists_ShouldDeleteTransaction() {
        // Arrange
        when(transactionRepository.findByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.of(testTransaction));

        // Act
        transactionService.deleteTransaction(userId, transactionId);

        // Assert
        verify(transactionRepository).findByIdAndUserId(transactionId, userId);
        verify(accountRepository).save(testAccount); // Balance revert
        verify(transactionRepository).delete(testTransaction);
    }

    @Test
    void deleteTransaction_WhenTransactionNotFound_ShouldThrowException() {
        // Arrange
        when(transactionRepository.findByIdAndUserId(transactionId, userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> transactionService.deleteTransaction(userId, transactionId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found");
    }
}
