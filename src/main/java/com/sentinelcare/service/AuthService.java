package com.sentinelcare.service;

import com.sentinelcare.dto.AuthRequest;
import com.sentinelcare.dto.AuthResponse;
import com.sentinelcare.security.JwtTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    public AuthResponse login(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );

        UserDetails principal = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenService.generateToken(principal);
        return new AuthResponse(token, principal.getUsername());
    }
}
