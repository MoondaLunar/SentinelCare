package com.sentinelcare.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtTokenService {

    private static final long EXPIRATION_MS = 900_000L;

    private final SecretKey secretKey;
    private final IdleSessionRegistry sessions;

    public JwtTokenService(@Value("${jwt.signing-key}") String signingKey, IdleSessionRegistry sessions) {
        this.sessions = sessions;
        if (signingKey == null || signingKey.isBlank()) {
            throw new IllegalStateException("JWT signing key must be configured via jwt.signing-key.");
        }
        this.secretKey = Keys.hmacShaKeyFor(signingKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        String sessionId = sessions.create(userDetails.getUsername());
        return Jwts.builder()
            .subject(userDetails.getUsername())
            .id(sessionId)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(secretKey)
            .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username != null && username.equals(userDetails.getUsername()) && !isExpired(token)
            && sessions.valid(sessionId(token), username);
    }

    public String sessionId(String token) {
        return extractClaim(token, Claims::getId);
    }

    public boolean recordUserActivity(String token, String username) {
        return username != null && username.equals(extractUsername(token))
            && !isExpired(token) && sessions.activity(sessionId(token), username);
    }

    public void logout(String token) {
        sessions.revoke(sessionId(token));
    }

    public boolean isExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return claimsResolver.apply(claims);
    }
}
