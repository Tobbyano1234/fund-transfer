package com.ecobank.fundtransferservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    @NotBlank(message = "Owner id is required")
    private UUID ownerId;

    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters (ISO code)")
    private String currency;
}

