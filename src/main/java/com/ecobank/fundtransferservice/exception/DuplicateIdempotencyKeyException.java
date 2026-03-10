package com.ecobank.fundtransferservice.exception;

public class DuplicateIdempotencyKeyException extends RuntimeException {

    private final String cachedResponse;

    public DuplicateIdempotencyKeyException(String message, String cachedResponse) {
        super(message);
        this.cachedResponse = cachedResponse;
    }

    public String getCachedResponse() {
        return cachedResponse;
    }
}

