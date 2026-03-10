package com.ecobank.fundtransferservice.controller;

import com.ecobank.fundtransferservice.model.dto.base.ApiResponse;
import com.ecobank.fundtransferservice.model.dto.request.LoginRequest;
import com.ecobank.fundtransferservice.model.dto.request.SignUpRequest;
import com.ecobank.fundtransferservice.model.dto.response.LoginResponse;
import com.ecobank.fundtransferservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/user/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody SignUpRequest request) {
        LoginResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "User registered successfully"));
    }

    @PostMapping("/user/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }
}
