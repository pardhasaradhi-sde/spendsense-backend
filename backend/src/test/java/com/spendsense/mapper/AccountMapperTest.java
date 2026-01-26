package com.spendsense.mapper;

import com.spendsense.dto.request.CreateAccountRequest;
import com.spendsense.dto.response.AccountResponse;
import com.spendsense.model.Account;
import com.spendsense.model.User;
import com.spendsense.model.enums.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountMapperTest {

    private AccountMapper accountMapper;
    private Account account;
    private CreateAccountRequest createRequest;
    private UUID accountId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        accountMapper = new AccountMapper();
        accountId = UUID.randomUUID();
        userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");

        account = new Account();
        account.setId(accountId);
        account.setUser(user);
        account.setName("Checking Account");
        account.setType(AccountType.CHECKING);
        account.setBalance(BigDecimal.valueOf(1000.00));
        account.setDefaultAccount(false);

        createRequest = new CreateAccountRequest();
        createRequest.setName("Checking Account");
        createRequest.setType(AccountType.CHECKING);
        createRequest.setBalance(BigDecimal.valueOf(1000.00));
        createRequest.setIsDefault(false);
    }

    @Test
    void toEntity_ShouldMapRequestToEntity() {
        // Act
        Account result = accountMapper.toEntity(createRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Checking Account");
        assertThat(result.getType()).isEqualTo(AccountType.CHECKING);
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        assertThat(result.isDefaultAccount()).isFalse();
    }

    @Test
    void toResponse_ShouldMapEntityToResponse() {
        // Act
        AccountResponse result = accountMapper.toResponse(account);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(accountId);
        assertThat(result.getName()).isEqualTo("Checking Account");
        assertThat(result.getType()).isEqualTo(AccountType.CHECKING);
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        assertThat(result.isDefaultAccount()).isFalse();
    }

    @Test
    void toEntity_WhenIsDefaultNull_ShouldSetToFalse() {
        // Arrange
        createRequest.setIsDefault(null);

        // Act
        Account result = accountMapper.toEntity(createRequest);

        // Assert
        assertThat(result.isDefaultAccount()).isFalse();
    }
}
