package com.sentinelcare.security;

import com.sentinelcare.web.ApiProblemWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String INVALID_TOKEN_DETAIL = "Invalid or expired JWT";

    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;
    private final ApiProblemWriter problemWriter;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService, UserDetailsService userDetailsService,
                                   ApiProblemWriter problemWriter) {
        this.jwtTokenService = jwtTokenService;
        this.userDetailsService = userDetailsService;
        this.problemWriter = problemWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            problemWriter.write(response, HttpStatus.UNAUTHORIZED, "Unauthorized", INVALID_TOKEN_DETAIL);
            return;
        }

        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) {
            problemWriter.write(response, HttpStatus.UNAUTHORIZED, "Unauthorized", INVALID_TOKEN_DETAIL);
            return;
        }

        try {
            String username = jwtTokenService.extractUsername(token);
            if (username == null || username.isBlank()) {
                problemWriter.write(response, HttpStatus.UNAUTHORIZED, "Unauthorized", INVALID_TOKEN_DETAIL);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtTokenService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } else {
                    problemWriter.write(response, HttpStatus.UNAUTHORIZED, "Unauthorized", INVALID_TOKEN_DETAIL);
                    return;
                }
            }
        } catch (RuntimeException ex) {
            problemWriter.write(response, HttpStatus.UNAUTHORIZED, "Unauthorized", INVALID_TOKEN_DETAIL);
            return;
        }

        filterChain.doFilter(request, response);
    }
}