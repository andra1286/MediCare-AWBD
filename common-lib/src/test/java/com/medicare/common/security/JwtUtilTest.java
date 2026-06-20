package com.medicare.common.security;

import com.medicare.common.dto.JwtClaims;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "medicare-super-secret-key-that-is-long-enough-256bit";
    private final JwtUtil jwtUtil = new JwtUtil(SECRET, 3600_000L);

    @Test
    void generatedTokenCanBeParsedBack() {
        JwtClaims original = new JwtClaims("dr.house", List.of("ROLE_DOCTOR"), 42L);

        String token = jwtUtil.generateToken(original);
        JwtClaims parsed = jwtUtil.parseToken(token);

        assertThat(parsed.getUsername()).isEqualTo("dr.house");
        assertThat(parsed.getRoles()).containsExactly("ROLE_DOCTOR");
        assertThat(parsed.getUserId()).isEqualTo(42L);
    }

    @Test
    void validTokenIsAccepted() {
        String token = jwtUtil.generateToken(new JwtClaims("alice", List.of("ROLE_PATIENT"), 1L));
        assertThat(jwtUtil.isValid(token)).isTrue();
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwtUtil.generateToken(new JwtClaims("alice", List.of("ROLE_PATIENT"), 1L));
        String tampered = token.substring(0, token.length() - 2) + "xx";
        assertThat(jwtUtil.isValid(tampered)).isFalse();
    }

    @Test
    void tokenSignedWithDifferentSecretIsRejected() {
        JwtUtil other = new JwtUtil("a-totally-different-secret-key-also-long-enough-256b", 3600_000L);
        String token = other.generateToken(new JwtClaims("mallory", List.of("ROLE_ADMIN"), 9L));
        assertThat(jwtUtil.isValid(token)).isFalse();
    }
}
