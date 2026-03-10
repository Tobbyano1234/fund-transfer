package com.ecobank.fundtransferservice.controller;

import com.ecobank.fundtransferservice.model.dto.base.ApiResponse;
import  com.ecobank.fundtransferservice.model.dto.request.TransactionRequest;
import  com.ecobank.fundtransferservice.model.dto.request.TransferRequest;
import  com.ecobank.fundtransferservice.model.dto.response.PaginatedTransactionResponse;
import  com.ecobank.fundtransferservice.model.dto.response.TransactionResponse;
import com.ecobank.fundtransferservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping()
    public ResponseEntity<ApiResponse<TransactionResponse>> processTransaction(
            @Valid @RequestBody TransactionRequest request) {
        log.info("Received {} transaction request for account: {}",
                request.getType(), request.getAccountId());

        TransactionResponse response = transactionService.processTransaction(request);

        return ResponseEntity.ok(ApiResponse.success(response, "Transaction processed successfully"));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> processTransfer(
            @Valid @RequestBody TransferRequest request) {
        log.info("Received transfer request from {} to {}",
                request.getFromAccountId(), request.getToAccountId());

        TransactionResponse response = transactionService.processTransfer(request);

        return ResponseEntity.ok(ApiResponse.success(response, "Transfer completed successfully"));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<ApiResponse<PaginatedTransactionResponse>> getTransactionHistory(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        log.info("Fetching transaction history for account: {}", accountId);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        PaginatedTransactionResponse transactions = transactionService.getTransactionHistory(accountId, pageable);

        return ResponseEntity.ok(ApiResponse.success(transactions));
    }
}
