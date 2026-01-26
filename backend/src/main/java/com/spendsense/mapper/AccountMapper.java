package com.spendsense.mapper;

import com.spendsense.dto.request.CreateAccountRequest;
import com.spendsense.dto.response.AccountResponse;
import com.spendsense.model.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    public Account toEntity(CreateAccountRequest request) {
        Account account=new Account();
        account.setName(request.getName());
        account.setType(request.getType());
        account.setBalance(request.getBalance());
        account.setDefaultAccount(request.getIsDefault()!=null?request.getIsDefault():false);
        return account;
    }

    public AccountResponse toResponse(Account account)
    {
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .balance(account.getBalance())
                .isDefault(account.isDefaultAccount())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
