package com.sentinelcare.controller;

import com.sentinelcare.dto.AuthRequest;
import com.sentinelcare.dto.AuthResponse;
import com.sentinelcare.service.AuthService;
import com.sentinelcare.security.JwtTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenService tokens;

    public AuthController(AuthService authService, JwtTokenService tokens) {
        this.authService = authService;
        this.tokens = tokens;
    }

    // Call only for genuine user interaction, never from a timer or background sync.
    @PostMapping("/auth/activity")
    public ResponseEntity<Void> activity(@org.springframework.web.bind.annotation.RequestHeader(HttpHeaders.AUTHORIZATION) String bearer,
                                         org.springframework.security.core.Authentication authentication) {
        if (!bearer.startsWith("Bearer ") || authentication == null
                || !tokens.recordUserActivity(bearer.substring(7).trim(), authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(@org.springframework.web.bind.annotation.RequestHeader(HttpHeaders.AUTHORIZATION) String bearer) {
        if (bearer.startsWith("Bearer ")) tokens.logout(bearer.substring(7).trim());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
