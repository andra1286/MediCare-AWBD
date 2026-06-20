package com.medicare.common.security;

import com.medicare.common.dto.JwtClaims;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Creates and validates JWTs using an HMAC-SHA256 secret.
 * <p>
 * The same secret is shared by all services, so any service can validate a token
 * locally (no network call to identity-service is needed just to check a token).
 * identity-service is the only one that <em>issues</em> tokens (after login).
 */
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMillis;

    /**
     * @param secret           shared signing secret (must be at least 32 chars for HS256)
     * @param expirationMillis token lifetime in milliseconds
     */
    public JwtUtil(String secret, long expirationMillis) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    /** Builds a signed token from the given claims. */
    public String generateToken(JwtClaims claims) {
        Date now = new Date();
        return Jwts.builder()
                .subject(claims.getUsername())
                .claim(SecurityConstants.ROLES_CLAIM, claims.getRoles())
                .claim(SecurityConstants.USER_ID_CLAIM, claims.getUserId())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMillis))
                .signWith(key)
                .compact();
    }

    /** Verifies the signature/expiry and extracts the claims. Throws if invalid. */
    @SuppressWarnings("unchecked")
    public JwtClaims parseToken(String token) {
        Claims body = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        List<String> roles = body.get(SecurityConstants.ROLES_CLAIM, List.class);
        Number rawUserId = body.get(SecurityConstants.USER_ID_CLAIM, Number.class);
        Long userId = rawUserId == null ? null : rawUserId.longValue();
        return new JwtClaims(body.getSubject(), roles, userId);
    }

    /** Returns true if the token is well-formed, correctly signed and not expired. */
    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
