package com.spendsense.service;

import com.spendsense.dto.request.CreateAccountRequest;
import com.spendsense.dto.request.UpdateAccountRequest;
import com.spendsense.dto.response.AccountResponse;
import com.spendsense.exception.ResourceNotFoundException;
import com.spendsense.mapper.AccountMapper;
import com.spendsense.model.Account;
import com.spendsense.model.User;
import com.spendsense.model.enums.AccountType;
import com.spendsense.repository.AccountRepository;
import com.spendsense.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;
    private AccountResponse accountResponse;
    private CreateAccountRequest createRequest;
    private UpdateAccountRequest updateRequest;
    private UUID userId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");
        testUser.setName("John Doe");

        testAccount = new Account();
        testAccount.setId(accountId);
        testAccount.setUser(testUser);
        testAccount.setName("Checking Account");
        testAccount.setType(AccountType.CHECKING);
        testAccount.setBalance(BigDecimal.valueOf(1000.00));
        testAccount.setDefaultAccount(false);

        accountResponse = new AccountResponse();
        accountResponse.setId(accountId);
        accountResponse.setName("Checking Account");
        accountResponse.setType(AccountType.CHECKING);
        accountResponse.setBalance(BigDecimal.valueOf(1000.00));

        createRequest = new CreateAccountRequest();
        createRequest.setName("Checking Account");
        createRequest.setType(AccountType.CHECKING);
        createRequest.setBalance(BigDecimal.valueOf(1000.00));
        createRequest.setIsDefault(false);

        updateRequest = new UpdateAccountRequest();
        updateRequest.setName("Updated Account");
        updateRequest.setType(AccountType.SAVINGS);
        updateRequest.setBalance(BigDecimal.valueOf(2000.00));
    }

    @Test
    void createAccount_WhenValidRequest_ShouldCreateAccount() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(accountMapper.toEntity(createRequest)).thenReturn(testAccount);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(accountMapper.toResponse(testAccount)).thenReturn(accountResponse);

        // Act
        AccountResponse result = accountService.createAccount(userId, createRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Checking Account");
        verify(userRepository).findById(userId);
        verify(accountRepository).save(any(Account.class));
        verify(accountMapper).toResponse(testAccount);
    }

    @Test
    void createAccount_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> accountService.createAccount(userId, createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User Not Found");
    }

    @Test
    void createAccount_WhenIsDefaultTrue_ShouldUnsetOtherDefaultAccounts() {
        // Arrange
        createRequest.setIsDefault(true);
        Account existingDefaultAccount = new Account();
        existingDefaultAccount.setId(UUID.randomUUID());
        existingDefaultAccount.setDefaultAccount(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(accountRepository.findByUserIdAndDefaultAccountTrue(userId))
                .thenReturn(Optional.of(existingDefaultAccount));
        when(accountMapper.toEntity(createRequest)).thenReturn(testAccount);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(accountMapper.toResponse(testAccount)).thenReturn(accountResponse);

        // Act
        AccountResponse result = accountService.createAccount(userId, createRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(accountRepository).findByUserIdAndDefaultAccountTrue(userId);
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void getUserAccounts_ShouldReturnAllUserAccounts() {
        // Arrange
        List<Account> accounts = Arrays.asList(testAccount);
        when(accountRepository.findByUserId(userId)).thenReturn(accounts);
        when(accountMapper.toResponse(any(Account.class))).thenReturn(accountResponse);

        // Act
        List<AccountResponse> result = accountService.getUserAccounts(userId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Checking Account");
        verify(accountRepository).findByUserId(userId);
    }

    @Test
    void getAccountById_WhenAccountExists_ShouldReturnAccount() {
        // Arrange
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(testAccount));
        when(accountMapper.toResponse(testAccount)).thenReturn(accountResponse);

        // Act
        AccountResponse result = accountService.getAccountById(userId, accountId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(accountId);
        verify(accountRepository).findByIdAndUserId(accountId, userId);
    }

    @Test
    void getAccountById_WhenAccountNotFound_ShouldThrowException() {
        // Arrange
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> accountService.getAccountById(userId, accountId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account Not Found");
    }

    @Test
    void updateAccount_WhenValidRequest_ShouldUpdateAccount() {
        // Arrange
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(accountMapper.toResponse(testAccount)).thenReturn(accountResponse);

        // Act
        AccountResponse result = accountService.updateAccount(userId, accountId, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(accountRepository).findByIdAndUserId(accountId, userId);
        verify(accountRepository).save(testAccount);
    }

    @Test
    void updateAccount_WhenAccountNotFound_ShouldThrowException() {
        // Arrange
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> accountService.updateAccount(userId, accountId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account Not Found");
    }

    @Test
    void updateAccount_WhenSettingAsDefault_ShouldUnsetOtherDefaults() {
        // Arrange
        updateRequest.setIsDefault(true);
        Account existingDefaultAccount = new Account();
        existingDefaultAccount.setId(UUID.randomUUID());
        existingDefaultAccount.setDefaultAccount(true);

        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.findByUserIdAndDefaultAccountTrue(userId))
                .thenReturn(Optional.of(existingDefaultAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(accountMapper.toResponse(testAccount)).thenReturn(accountResponse);

        // Act
        AccountResponse result = accountService.updateAccount(userId, accountId, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void deleteAccount_WhenAccountExists_ShouldDeleteAccount() {
        // Arrange
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(testAccount));

        // Act
        accountService.deleteAccount(userId, accountId);

        // Assert
        verify(accountRepository).findByIdAndUserId(accountId, userId);
        verify(accountRepository).delete(testAccount);
    }

    @Test
    void deleteAccount_WhenAccountNotFound_ShouldThrowException() {
        // Arrange
        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> accountService.deleteAccount(userId, accountId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Account Not Found");
    }
}
