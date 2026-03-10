package com.ecobank.fundtransferservice.service.impl;

import com.ecobank.fundtransferservice.model.dto.request.CreateAccountRequest;
import com.ecobank.fundtransferservice.model.dto.response.AccountResponse;
import com.ecobank.fundtransferservice.exception.ResourceNotFoundException;
import com.ecobank.fundtransferservice.model.domain.Account;
import com.ecobank.fundtransferservice.repository.AccountRepository;
import com.ecobank.fundtransferservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating account for owner: {}", request.getOwnerId());

        Account account = Account.builder()
                .ownerId(request.getOwnerId())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .build();

        account = accountRepository.save(account);
        log.info("Account created with ID: {}", account.getId());

        return com.ecobank.fundtransferservice.model.dto.response.AccountResponse.fromEntity(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));

        return AccountResponse.fromEntity(account);
    }
}
