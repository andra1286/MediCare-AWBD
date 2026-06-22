package com.medicare.webui.security;

import com.medicare.common.dto.JwtClaims;
import com.medicare.common.security.JwtUtil;
import com.medicare.webui.client.IdentityApiClient;
import com.medicare.webui.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

/**
 * Validates credentials against identity-service {@code POST /auth/login} and stores the JWT
 * in {@link Authentication#getDetails()} for downstream API calls (credentials are erased after login).
 */
@Component
@RequiredArgsConstructor
public class IdentityAuthenticationProvider implements AuthenticationProvider {

    private final IdentityApiClient identityApiClient;
    private final JwtUtil jwtUtil;

    @Override
    public Authentication authenticate(Authentication authentication) {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        try {
            LoginResponse response = identityApiClient.login(username, password);
            JwtClaims claims = jwtUtil.parseToken(response.getToken());
            List<SimpleGrantedAuthority> authorities = claims.getRoles().stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            return JwtAuthenticationSupport.authenticated(username, response.getToken(), authorities);
        } catch (RestClientResponseException ex) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
