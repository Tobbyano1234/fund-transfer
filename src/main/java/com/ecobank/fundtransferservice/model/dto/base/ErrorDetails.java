package com.ecobank.fundtransferservice.model.dto.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetails {
    private String code; // Error code (e.g., "VALIDATION_ERROR")
    private String description; // Detailed error description
    private List<String> fieldErrors; // Validation field errors
    private Map<String, String> details; // Additional error details
}
