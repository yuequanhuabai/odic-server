package com.oidc.server.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret:this-is-a-very-secret-key-that-should-be-at-least-256-bits-long-for-hs256}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateAccessToken(Long userId, String username, String clientId) {
        return generateTokenWithClaims(userId, username, clientId, jwtExpirationMs);
    }

    public String generateIdToken(Long userId, String username, String email, String clientId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("client_id", clientId);

        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .claim("sub", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId, String username) {
        return Jwts.builder()
                .subject(username)
                .claim("sub", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) // 7 days
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateTokenWithClaims(Long userId, String username, String clientId, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("client_id", clientId);

        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .claim("sub", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        return extractClaim(token, claims -> Long.parseLong(claims.get("sub").toString()));
    }

    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String getClientIdFromToken(String token) {
        return extractClaim(token, claims -> (String) claims.get("client_id"));
    }

    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
