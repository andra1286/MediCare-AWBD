package com.medicare.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicare.common.dto.JwtClaims;
import com.medicare.common.security.JwtUtil;
import com.medicare.common.security.SecurityConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtAuthenticationFilterTest {

    private static final String SECRET = "MedicareTestSecretKeyMustBeAtLeast32Chars!!";

    @Mock
    private GatewayFilterChain chain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(new JwtUtil(SECRET, 3600000), new ObjectMapper());
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void allowsPublicLoginPathWithoutToken() {
        ServerWebExchange exchange = exchangeFor("/identity/auth/login", null);

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void rejectsProtectedRouteWithoutToken() {
        ServerWebExchange exchange = exchangeFor("/appointments/api/appointments", null);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void forwardsClaimsForValidToken() {
        String token = new JwtUtil(SECRET, 3600000)
                .generateToken(new JwtClaims("doctor", List.of("ROLE_DOCTOR"), 2L));
        ServerWebExchange exchange = exchangeFor("/appointments/api/appointments",
                SecurityConstants.BEARER_PREFIX + token);

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        filter.filter(exchange, chain).block();

        verify(chain).filter(captor.capture());
        assertThat(captor.getValue().getRequest().getHeaders().getFirst(SecurityConstants.USER_HEADER))
                .isEqualTo("doctor");
    }

    private ServerWebExchange exchangeFor(String path, String authorization) {
        MockServerHttpRequest.BaseBuilder<?> builder = MockServerHttpRequest.get(path);
        if (authorization != null) {
            builder.header(HttpHeaders.AUTHORIZATION, authorization);
        }
        return MockServerWebExchange.from(builder.build());
    }
}
