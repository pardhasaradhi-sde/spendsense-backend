package com.spendsense.repository;

import com.spendsense.model.Account;
import com.spendsense.model.User;
import com.spendsense.model.enums.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setClerkUserId("clerk_123");
        testUser.setEmail("test@example.com");
        testUser.setName("John Doe");
        testUser = entityManager.persist(testUser);

        testAccount = new Account();
        testAccount.setUser(testUser);
        testAccount.setName("Checking Account");
        testAccount.setType(AccountType.CHECKING);
        testAccount.setBalance(BigDecimal.valueOf(1000.00));
        testAccount.setDefaultAccount(false);
        testAccount = entityManager.persist(testAccount);

        entityManager.flush();
    }

    @Test
    void findByUserId_ShouldReturnUserAccounts() {
        // Act
        List<Account> accounts = accountRepository.findByUserId(testUser.getId());

        // Assert
        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getName()).isEqualTo("Checking Account");
        assertThat(accounts.get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void findByIdAndUserId_WhenAccountExists_ShouldReturnAccount() {
        // Act
        Optional<Account> result = accountRepository.findByIdAndUserId(
                testAccount.getId(), testUser.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Checking Account");
    }

    @Test
    void findByIdAndUserId_WhenAccountDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setClerkUserId("clerk_456");
        anotherUser.setEmail("another@example.com");
        anotherUser.setName("Jane Doe");
        anotherUser = entityManager.persist(anotherUser);

        // Act
        Optional<Account> result = accountRepository.findByIdAndUserId(
                testAccount.getId(), anotherUser.getId());

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findByUserIdAndDefaultAccountTrue_ShouldReturnDefaultAccount() {
        // Arrange
        testAccount.setDefaultAccount(true);
        entityManager.persist(testAccount);
        entityManager.flush();

        // Act
        Optional<Account> result = accountRepository.findByUserIdAndDefaultAccountTrue(testUser.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().isDefaultAccount()).isTrue();
    }

    @Test
    void findByUserIdAndDefaultAccountTrue_WhenNoDefaultAccount_ShouldReturnEmpty() {
        // Act
        Optional<Account> result = accountRepository.findByUserIdAndDefaultAccountTrue(testUser.getId());

        // Assert
        assertThat(result).isEmpty();
    }
}
