package com.ecobank.fundtransferservice.model.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int statusCode;
    private Boolean success;
    private String message;
    private T payload;
    private Object error;
    private String token;
    private LocalDateTime timestamp;
    private String path;

    // Success response methods
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .statusCode(200)
                .success(true)
                .message("Request processed successfully")
                .payload(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .statusCode(200)
                .success(true)
                .message(message)
                .payload(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .statusCode(201)
                .success(true)
                .message(message)
                .payload(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Error response methods
    public static <T> ApiResponse<T> error(int statusCode, String message) {
        return ApiResponse.<T>builder()
                .statusCode(statusCode)
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(int statusCode, String message, Object error) {
        return ApiResponse.<T>builder()
                .statusCode(statusCode)
                .success(false)
                .message(message)
                .error(error)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return error(400, message);
    }

    public static <T> ApiResponse<T> unauthorized(String message) {
        return error(401, message);
    }

    public static <T> ApiResponse<T> forbidden(String message) {
        return error(403, message);
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return error(404, message);
    }

    public static <T> ApiResponse<T> internalServerError(String message) {
        return error(500, message);
    }

    // Authentication response with token
    public static <T> ApiResponse<T> successWithToken(T data, String token, String message) {
        return ApiResponse.<T>builder()
                .statusCode(200)
                .success(true)
                .message(message)
                .payload(data)
                .token(token)
                .timestamp(LocalDateTime.now())
                .build();
    }
}