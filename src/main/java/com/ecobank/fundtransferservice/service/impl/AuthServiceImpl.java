package com.ecobank.fundtransferservice.service.impl;

import com.ecobank.fundtransferservice.enums.Role;
import com.ecobank.fundtransferservice.exception.UserException;
import com.ecobank.fundtransferservice.model.domain.User;
import com.ecobank.fundtransferservice.model.dto.request.LoginRequest;
import com.ecobank.fundtransferservice.model.dto.request.SignUpRequest;
import com.ecobank.fundtransferservice.model.dto.response.LoginResponse;
import com.ecobank.fundtransferservice.repository.UserRepository;
import com.ecobank.fundtransferservice.security.UserDetailsServiceImpl;
import com.ecobank.fundtransferservice.service.AuthService;
import com.ecobank.fundtransferservice.utils.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class  AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public LoginResponse register(SignUpRequest signUpRequest) {
        String email = signUpRequest.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserException("User with email " + email + " already exists");
        }

        User user = User.builder()
                .fullName(signUpRequest.getFullName().trim())
                .email(email)
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = generateToken(savedUser, userDetails);

        log.info("Registered new user: {}", savedUser.getEmail());
        return buildLoginResponse(savedUser, token);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Attempting to authenticate user: {}", email);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for user: {}", email);
            throw new UserException("Invalid email or password");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException("User not found"));
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token = generateToken(user, userDetails);

        log.info("User {} authenticated successfully", email);
        return buildLoginResponse(user, token);
    }

    private String generateToken(User user, UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId().toString());
        return jwtTokenUtil.generateToken(userDetails, claims);
    }

    private LoginResponse buildLoginResponse(User user, String token) {
        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId().toString())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
