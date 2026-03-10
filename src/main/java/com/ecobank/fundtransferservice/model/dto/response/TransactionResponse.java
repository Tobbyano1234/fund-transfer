package com.ecobank.fundtransferservice.model.dto.response;

import com.ecobank.fundtransferservice.enums.TransactionStatus;
import com.ecobank.fundtransferservice.enums.TransactionType;
import com.ecobank.fundtransferservice.model.domain.LedgerEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private UUID id;
    private UUID accountId;
    private Long amount;
    private TransactionType transactionType;
    private TransactionStatus status;
    private String idempotencyKey;
    private String description;
    private UUID relatedAccountId;
    private Long balanceAfter;
    private LocalDateTime createdAt;

    public static TransactionResponse fromEntry(LedgerEntry entry) {
        return TransactionResponse.builder()
                .id(entry.getId())
                .accountId(entry.getAccountId())
                .amount(Math.abs(entry.getAmount()))
                .transactionType(entry.getTransactionType())
                .status(entry.getStatus())
                .idempotencyKey(entry.getIdempotencyKey())
                .description(entry.getDescription())
                .relatedAccountId(entry.getRelatedAccountId())
                .balanceAfter(entry.getBalanceAfter())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}

