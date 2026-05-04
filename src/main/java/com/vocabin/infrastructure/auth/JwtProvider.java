package com.vocabin.infrastructure.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiryMs;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry-ms:1800000}") long accessTokenExpiryMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryMs = accessTokenExpiryMs;
    }

    public String generateAccessToken(Long memberId) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(new Date(now))
                .expiration(new Date(now + accessTokenExpiryMs))
                .signWith(secretKey)
                .compact();
    }

    public Long extractMemberId(String token) {
        String subject = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Long.valueOf(subject);
    }

    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
