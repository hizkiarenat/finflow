package com.finflow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finflow.dto.ApiResponse;
import com.finflow.dto.LoginRequest;
import com.finflow.dto.LoginResponse;
import com.finflow.dto.UserResponse;
import com.finflow.model.User;
import com.finflow.repository.UserRepository;
import com.finflow.security.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AuthController — handle login dan generate JWT token
 *
 * POST /api/v1/auth/login → return JWT token
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login attempt for email: {}", request.getEmail());

        try {
            // 1. Validasi email & password via Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase(),
                            request.getPassword()
                    )
            );

            // 2. Ambil data user dari DB
            User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                    .orElseThrow();

            // 3. Cek user masih ACTIVE
            if (user.getStatus() != User.UserStatus.ACTIVE) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Account is not active"));
            }

            // 4. Generate JWT token
            String token = jwtUtil.generateToken(user.getId(), user.getEmail());

            // 5. Build response
            LoginResponse loginResponse = LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiredAt(jwtUtil.extractExpiredAt(token))
                    .userResponse(UserResponse.fromEntity(user))
                    .build();

            log.info("Login successful for userId: {}", user.getId());

            return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));

        } catch (BadCredentialsException e) {
            log.warn("Login failed for email: {}", request.getEmail());
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid email or password"));
        }
    }
}
