package com.medicare.webui.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

/** Keeps the JWT in {@link Authentication#getDetails()} so it survives credential erasure after login. */
public final class JwtAuthenticationSupport {

    private JwtAuthenticationSupport() {
    }

    public static UsernamePasswordAuthenticationToken authenticated(
            String username, String jwt, Collection<SimpleGrantedAuthority> authorities) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(username, "", authorities);
        token.setDetails(jwt);
        return token;
    }

    public static String resolveJwt(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        if (authentication.getDetails() instanceof String jwt && !jwt.isBlank()) {
            return jwt;
        }
        if (authentication.getCredentials() instanceof String jwt && !jwt.isBlank()) {
            return jwt;
        }
        return null;
    }
}
