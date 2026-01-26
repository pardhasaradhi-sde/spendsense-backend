package com.spendsense.service;

import com.spendsense.dto.request.CreateAccountRequest;
import com.spendsense.dto.request.UpdateAccountRequest;
import com.spendsense.dto.response.AccountResponse;
import com.spendsense.exception.ResourceNotFoundException;
import com.spendsense.mapper.AccountMapper;
import com.spendsense.model.Account;
import com.spendsense.model.User;
import com.spendsense.repository.AccountRepository;
import com.spendsense.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;
    @PersistenceContext
    private EntityManager entityManager;
    public AccountResponse createAccount(UUID userId, CreateAccountRequest request){
        User user=userRepository.findById(userId).
                orElseThrow(()->new ResourceNotFoundException("User Not Found"));
        if(request.getIsDefault()!=null && request.getIsDefault())
        {
            accountRepository.findByUserIdAndDefaultAccountTrue(userId)
                    .ifPresent(account -> {
                        account.setDefaultAccount(false);
                        accountRepository.save(account);
                    });
        }
        Account account=accountMapper.toEntity(request);
        account.setUser(user);
        Account saved = accountRepository.save(account);
        accountRepository.flush();
        entityManager.refresh(saved);
        return accountMapper.toResponse(saved);
    }

    public List<AccountResponse> getUserAccounts(UUID userId){
        return accountRepository.findByUserId(userId)
                .stream()
                .map(accountMapper::toResponse)
                .collect(Collectors.toList());
    }


    public AccountResponse getAccountById(UUID userId,UUID accountId){
        Account account=accountRepository.findByIdAndUserId(accountId,userId)
                .orElseThrow(()->new ResourceNotFoundException("Account Not Found"));
        return accountMapper.toResponse(account);
    }


    public AccountResponse updateAccount(UUID userId, UUID accountId, UpdateAccountRequest request){
        Account account=accountRepository.findByIdAndUserId(accountId,userId)
                .orElseThrow(()->new ResourceNotFoundException("Account Not Found"));
        if(request.getName()!=null){
            account.setName(request.getName());
        }
        if(request.getType()!=null)
        {
            account.setType(request.getType());
        }
        if(request.getBalance()!=null)
        {
            account.setBalance(request.getBalance());
        }
        if(request.getIsDefault()!=null && request.getIsDefault())
        {
            accountRepository.findByUserIdAndDefaultAccountTrue(userId)
                    .ifPresent(defaultAccount->{
                        if(!defaultAccount.getId().equals(accountId)){
                            defaultAccount.setDefaultAccount(false);
                            accountRepository.save(defaultAccount);
                        }
                    });
            account.setDefaultAccount(true);
        }
        Account Updated=accountRepository.save(account);
        return accountMapper.toResponse(Updated);
    }


    public void deleteAccount(UUID userId,UUID accountId)
    {
        Account account=accountRepository.findByIdAndUserId(accountId,userId)
                .orElseThrow(()->new ResourceNotFoundException("Account Not Found"));

        accountRepository.delete(account);
    }
}
