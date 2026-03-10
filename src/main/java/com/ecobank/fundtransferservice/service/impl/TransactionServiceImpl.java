package com.ecobank.fundtransferservice.service.impl;

import com.ecobank.fundtransferservice.enums.TransactionStatus;
import com.ecobank.fundtransferservice.enums.TransactionType;
import com.ecobank.fundtransferservice.exception.AccountException;
import com.ecobank.fundtransferservice.exception.DuplicateIdempotencyKeyException;
import com.ecobank.fundtransferservice.exception.InsufficientFundsException;
import com.ecobank.fundtransferservice.exception.ResourceNotFoundException;
import com.ecobank.fundtransferservice.model.domain.Account;
import com.ecobank.fundtransferservice.model.domain.IdempotencyRecord;
import com.ecobank.fundtransferservice.model.domain.LedgerEntry;
import com.ecobank.fundtransferservice.model.dto.request.TransactionRequest;
import com.ecobank.fundtransferservice.model.dto.request.TransferRequest;
import com.ecobank.fundtransferservice.model.dto.response.PaginatedTransactionResponse;
import com.ecobank.fundtransferservice.model.dto.response.PaginationResponse;
import com.ecobank.fundtransferservice.model.dto.response.TransactionResponse;
import com.ecobank.fundtransferservice.repository.AccountRepository;
import com.ecobank.fundtransferservice.repository.IdempotencyRepository;
import com.ecobank.fundtransferservice.repository.LedgerRepository;
import com.ecobank.fundtransferservice.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final LedgerRepository ledgerRepository;
    private final IdempotencyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public TransactionResponse processTransaction(TransactionRequest request) {
        log.info("Processing {} transaction for account: {}", request.getType(), request.getAccountId());

        String idempotencyKey = UUID.randomUUID().toString();
        log.debug("Generated idempotency key: {}", idempotencyKey);

        checkAndHandleIdempotency(idempotencyKey);

        Account account = accountRepository.findByIdWithLock(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.getAccountId()));

        if (!account.canTransact()) {
            throw new AccountException("Account is not active");
        }

        TransactionType txType = parseTransactionType(request.getType());
        long signedAmount = computeSignedAmount(request.getAmount(), txType);
        long newBalance = account.getBalance() + signedAmount;

        if (newBalance < 0) {
            throw new InsufficientFundsException(
                    String.format("Debit of %d exceeds available balance %d",
                            request.getAmount(), account.getBalance()));
        }

        account.setBalance(newBalance);
        accountRepository.save(account);

        LedgerEntry entry = LedgerEntry.builder()
                .accountId(account.getId())
                .amount(signedAmount)
                .transactionType(txType)
                .status(TransactionStatus.COMPLETED)
                .idempotencyKey(idempotencyKey)
                .description(request.getDescription())
                .balanceAfter(newBalance)
                .build();

        entry = ledgerRepository.save(entry);

        TransactionResponse response = TransactionResponse.fromEntry(entry);
        storeIdempotencyRecord(idempotencyKey, response);

        log.info("Transaction completed: {} for account {}", entry.getId(), account.getId());
        return response;
    }

    @Override
    @Transactional
    public TransactionResponse processTransfer(TransferRequest request) {
        log.info("Processing transfer from {} to {}", request.getFromAccountId(), request.getToAccountId());

        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new AccountException("Cannot transfer to the same account");
        }

        String idempotencyKey = UUID.randomUUID().toString();
        log.debug("Generated idempotency key: {}", idempotencyKey);

        checkAndHandleIdempotency(idempotencyKey);

        UUID firstId = request.getFromAccountId().compareTo(request.getToAccountId()) < 0
                ? request.getFromAccountId() : request.getToAccountId();
        UUID secondId = request.getFromAccountId().compareTo(request.getToAccountId()) < 0
                ? request.getToAccountId() : request.getFromAccountId();

        Account firstAccount = accountRepository.findByIdWithLock(firstId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + firstId));
        Account secondAccount = accountRepository.findByIdWithLock(secondId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + secondId));

        Account sender = request.getFromAccountId().equals(firstId) ? firstAccount : secondAccount;
        Account receiver = request.getToAccountId().equals(firstId) ? firstAccount : secondAccount;

        if (!sender.canTransact() || !receiver.canTransact()) {
            throw new AccountException("One or both accounts are not active");
        }

        if (!sender.hasSufficientBalance(request.getAmount())) {
            throw new InsufficientFundsException(
                    String.format("Transfer of %d exceeds sender balance %d",
                            request.getAmount(), sender.getBalance()));
        }

        long senderNewBalance = sender.getBalance() - request.getAmount();
        long receiverNewBalance = receiver.getBalance() + request.getAmount();

        sender.setBalance(senderNewBalance);
        receiver.setBalance(receiverNewBalance);

        accountRepository.save(sender);
        accountRepository.save(receiver);

        String desc = request.getDescription() != null ? request.getDescription() : "Transfer";

        LedgerEntry debitEntry = LedgerEntry.builder()
                .accountId(sender.getId())
                .amount(-request.getAmount())
                .transactionType(TransactionType.TRANSFER_OUT)
                .status(TransactionStatus.COMPLETED)
                .idempotencyKey(idempotencyKey + "_out")
                .description(desc)
                .relatedAccountId(receiver.getId())
                .balanceAfter(senderNewBalance)
                .build();

        LedgerEntry creditEntry = LedgerEntry.builder()
                .accountId(receiver.getId())
                .amount(request.getAmount())
                .transactionType(TransactionType.TRANSFER_IN)
                .status(TransactionStatus.COMPLETED)
                .idempotencyKey(idempotencyKey + "_in")
                .description(desc)
                .relatedAccountId(sender.getId())
                .balanceAfter(receiverNewBalance)
                .build();

        ledgerRepository.save(debitEntry);
        creditEntry = ledgerRepository.save(creditEntry);

        TransactionResponse response = TransactionResponse.fromEntry(creditEntry);
        storeIdempotencyRecord(idempotencyKey, response);

        log.info("Transfer completed from {} to {}", sender.getId(), receiver.getId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedTransactionResponse getTransactionHistory(UUID accountId, Pageable pageable) {
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Account not found: " + accountId);
        }

        Page<TransactionResponse> page = ledgerRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable)
                .map(TransactionResponse::fromEntry);

        PaginationResponse pagination = PaginationResponse.builder()
                .pageNumber(page.getNumber() + 1)
                .pageSize(page.getSize())
                .hasNextPage(page.hasNext())
                .hasPrevPage(page.hasPrevious())
                .totalPage(page.getTotalPages())
                .total(page.getTotalElements())
                .build();

        return PaginatedTransactionResponse.builder()
                .content(page.getContent())
                .pagination(pagination)
                .build();
    }

    private TransactionType parseTransactionType(String type) {
        return switch (type.toUpperCase()) {
            case "CREDIT" ->
                TransactionType.CREDIT;
            case "DEBIT" ->
                TransactionType.DEBIT;
            default ->
                throw new AccountException("Invalid transaction type: " + type + ". Use CREDIT or DEBIT");
        };
    }

    private long computeSignedAmount(long amount, TransactionType type) {
        return switch (type) {
            case CREDIT, TRANSFER_IN ->
                amount;
            case DEBIT, TRANSFER_OUT ->
                -amount;
        };
    }

    private void checkAndHandleIdempotency(String key) {
        idempotencyRepository.findByIdemKey(key).ifPresent(record -> {
            throw new DuplicateIdempotencyKeyException(
                    "Transaction already processed with this idempotency key",
                    record.getResponsePayload());
        });
    }

    private void storeIdempotencyRecord(String key, TransactionResponse response) {
        try {
            String payload = objectMapper.writeValueAsString(response);
            IdempotencyRecord record = IdempotencyRecord.builder()
                    .idemKey(key)
                    .responsePayload(payload)
                    .httpStatus(200)
                    .build();
            idempotencyRepository.save(record);
        } catch (Exception e) {
            log.warn("Failed to store idempotency record: {}", e.getMessage());
        }
    }
}
