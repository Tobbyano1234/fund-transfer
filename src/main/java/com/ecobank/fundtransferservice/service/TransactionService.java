package com.ecobank.fundtransferservice.service;

import com.ecobank.fundtransferservice.model.dto.request.TransactionRequest;
import com.ecobank.fundtransferservice.model.dto.request.TransferRequest;
import com.ecobank.fundtransferservice.model.dto.response.PaginatedTransactionResponse;
import com.ecobank.fundtransferservice.model.dto.response.TransactionResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TransactionService {

    /**
     * Process credit or debit transaction
     */
    TransactionResponse processTransaction(TransactionRequest request);

    /**
     * Transfer funds between two account atomically
     */
    TransactionResponse processTransfer(TransferRequest request);

    /**
     * Get transaction history for an account
     */
    PaginatedTransactionResponse getTransactionHistory(UUID accountId, Pageable pageable);
}
