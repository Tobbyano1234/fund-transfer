package com.ecobank.fundtransferservice.service;

import com.ecobank.fundtransferservice.model.dto.request.CreateAccountRequest;
import com.ecobank.fundtransferservice.model.dto.response.AccountResponse;

import java.util.UUID;

public interface AccountService {

    /**
     * Create a new account
     */
    AccountResponse createAccount(CreateAccountRequest request);

    /**
     * Get account by ID
     */
    AccountResponse getAccount(UUID accountId);
}
