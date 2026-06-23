package com.medicare.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicare.common.dto.JwtClaims;
import com.medicare.common.error.ErrorResponse;
import com.medicare.common.security.JwtUtil;
import com.medicare.common.security.SecurityConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

/**
 * Validates JWT on protected API routes and forwards identity claims downstream.
 * Public: login/register, actuator, static web-ui assets, and the web-ui login page.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    public static final String ROLES_HEADER = "X-Auth-Roles";
    public static final String USER_ID_HEADER = "X-Auth-User-Id";

    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/identity/auth/",
            "/actuator/",
            "/login",
            "/css/",
            "/js/",
            "/webjars/",
            "/error"
    );

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String header = exchange.getRequest().getHeaders().getFirst(SecurityConstants.AUTH_HEADER);
        if (header == null || !header.startsWith(SecurityConstants.BEARER_PREFIX)) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = header.substring(SecurityConstants.BEARER_PREFIX.length());
        if (!jwtUtil.isValid(token)) {
            return unauthorized(exchange, "Invalid or expired token");
        }

        JwtClaims claims = jwtUtil.parseToken(token);
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header(SecurityConstants.USER_HEADER, claims.getUsername())
                .header(USER_ID_HEADER, String.valueOf(claims.getUserId()))
                .header(ROLES_HEADER, String.join(",", claims.getRoles()))
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private boolean isPublic(String path) {
        if ("/".equals(path)) {
            return true;
        }
        return PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message(message)
                .path(exchange.getRequest().getPath().pathWithinApplication().value())
                .build();
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            byte[] bytes = ("{\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
