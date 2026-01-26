package com.spendsense.repository;

import com.spendsense.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    //find all accounts of a user
    List<Account> findByUserId(UUID userId);

    //find user's default account
    Optional<Account> findByUserIdAndDefaultAccountTrue(UUID userId);

    //find specific account for a user
    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    //count user's accounts
    long countByUserId(UUID userId);

    // Custom query example (optional - for learning)
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId AND a.balance > :minBalance")
    List<Account> findAccountsWithMinBalance(@Param("userId") UUID userId,
                                             @Param("minBalance") java.math.BigDecimal minBalance);

}
