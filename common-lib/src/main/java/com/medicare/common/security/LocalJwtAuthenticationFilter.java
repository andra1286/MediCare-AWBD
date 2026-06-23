package com.medicare.common.security;

import com.medicare.common.dto.JwtClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Parses a Bearer JWT from the incoming request and populates the Spring Security context.
 * Used by business microservices for local token validation (BR-4).
 */
@RequiredArgsConstructor
public class LocalJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(SecurityConstants.AUTH_HEADER);
        if (header != null && header.startsWith(SecurityConstants.BEARER_PREFIX)) {
            String token = header.substring(SecurityConstants.BEARER_PREFIX.length());
            if (jwtUtil.isValid(token)) {
                JwtClaims claims = jwtUtil.parseToken(token);
                List<SimpleGrantedAuthority> authorities = claims.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();
                var authentication = new UsernamePasswordAuthenticationToken(
                        claims.getUsername(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
