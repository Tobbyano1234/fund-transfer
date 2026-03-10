package com.ecobank.fundtransferservice.controller;

import com.ecobank.fundtransferservice.model.dto.base.ApiResponse;
import com.ecobank.fundtransferservice.model.dto.request.CreateAccountRequest;
import com.ecobank.fundtransferservice.model.dto.response.AccountResponse;
import com.ecobank.fundtransferservice.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        log.info("Received request to create account for: {}", request.getOwnerId());

        AccountResponse account = accountService.createAccount(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(account, "Account created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(@PathVariable UUID id) {
        log.info("Received request to get account: {}", id);

        AccountResponse account = accountService.getAccount(id);

        return ResponseEntity.ok(ApiResponse.success(account));
    }
}

