package com.ecobank.fundtransferservice.controller.advice;

import com.ecobank.fundtransferservice.exception.AccountException;
import com.ecobank.fundtransferservice.exception.DuplicateIdempotencyKeyException;
import com.ecobank.fundtransferservice.exception.InsufficientFundsException;
import com.ecobank.fundtransferservice.exception.ResourceNotFoundException;
import com.ecobank.fundtransferservice.exception.UserException;
import com.ecobank.fundtransferservice.model.dto.base.ApiResponse;
import com.ecobank.fundtransferservice.model.dto.base.ErrorDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<String> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("VALIDATION_ERROR")
                .description("Input validation failed")
                .fieldErrors(fieldErrors)
                .build();

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .success(false)
                .message("Validation failed")
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        String fieldError = ex.getParameterName() + ": is required";
        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("VALIDATION_ERROR")
                .description("Missing required request parameter")
                .fieldErrors(List.of(fieldError))
                .build();

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .success(false)
                .message("Validation failed")
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "valid type";
        String fieldError = ex.getName() + ": must be of type " + requiredType;
        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("VALIDATION_ERROR")
                .description("Invalid request parameter type")
                .fieldErrors(List.of(fieldError))
                .build();

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .success(false)
                .message("Validation failed")
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserException(
            UserException ex, HttpServletRequest request) {

        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("USER_ERROR")
                .description(ex.getMessage())
                .build();

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .success(false)
                .message("User operation failed")
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccountException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountException(
            AccountException ex, HttpServletRequest request) {

        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("ACCOUNT_ERROR")
                .description(ex.getMessage())
                .build();

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .success(false)
                .message("Program operation failed")
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ApiResponse<Object>> handleInsufficientFundsException(
            InsufficientFundsException ex, HttpServletRequest request) {

        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("INSUFFICIENT_FUNDS")
                .description(ex.getMessage())
                .build();

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .success(false)
                .message("Transaction failed")
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DuplicateIdempotencyKeyException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateIdempotencyKeyException(
            DuplicateIdempotencyKeyException ex, HttpServletRequest request) {

        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("DUPLICATE_IDEMPOTENCY_KEY")
                .description(ex.getMessage())
                .details(Map.of("cachedResponse", ex.getCachedResponse()))
                .build();

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .statusCode(HttpStatus.CONFLICT.value())
                .success(false)
                .message("Duplicate transaction")
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("RESOURCE_NOT_FOUND")
                .description(ex.getMessage())
                .build();

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .success(false)
                .message("Resource not found")
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            HttpServletRequest request) {

        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("ACCESS_DENIED")
                .description("You do not have permission to access this resource")
                .build();

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .success(false)
                .message("Access denied")
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(
            HttpServletRequest request) {

        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("DATA_INTEGRITY_ERROR")
                .description("Resource already exists or violates data constraints")
                .build();

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .statusCode(HttpStatus.CONFLICT.value())
                .success(false)
                .message("Conflict")
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFoundException(
            HttpServletRequest request) {

        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("ENDPOINT_NOT_FOUND")
                .description("The requested API endpoint does not exist")
                .build();

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .success(false)
                .message("API endpoint not found")
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandlerFoundException(
            HttpServletRequest request) {

        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("ENDPOINT_NOT_FOUND")
                .description("The requested API endpoint does not exist")
                .build();

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .success(false)
                .message("API endpoint not found")
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error for request: {}", request.getRequestURI(), ex);

        ErrorDetails errorDetails = ErrorDetails.builder()
                .code("INTERNAL_SERVER_ERROR")
                .description("An unexpected error occurred. Please try again later.")
                .build();

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .success(false)
                .message("Internal server error")
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
