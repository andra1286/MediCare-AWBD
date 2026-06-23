package com.medicare.webui.security;

import com.medicare.common.security.SecurityConstants;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Attaches the session JWT to outbound RestClient calls. */
@Component
public class JwtBearerInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            org.springframework.http.HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String jwt = JwtAuthenticationSupport.resolveJwt(auth);
        if (jwt != null) {
            request.getHeaders().set(SecurityConstants.AUTH_HEADER,
                    SecurityConstants.BEARER_PREFIX + jwt);
        }
        return execution.execute(request, body);
    }
}
