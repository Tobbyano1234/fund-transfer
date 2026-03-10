package com.ecobank.fundtransferservice.service;

import com.ecobank.fundtransferservice.model.dto.request.LoginRequest;
import com.ecobank.fundtransferservice.model.dto.request.SignUpRequest;
import com.ecobank.fundtransferservice.model.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse register(SignUpRequest signUpRequest);

    LoginResponse login(LoginRequest request);
}
