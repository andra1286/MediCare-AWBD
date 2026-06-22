package com.medicare.common.feign;

import com.medicare.common.security.SecurityConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Propagates the inbound {@code Authorization} header to outbound Feign calls
 * so downstream services see the same authenticated user.
 */
public class JwtFeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            String auth = servletAttrs.getRequest().getHeader(SecurityConstants.AUTH_HEADER);
            if (auth != null && !auth.isBlank()) {
                template.header(SecurityConstants.AUTH_HEADER, auth);
            }
        }
    }
}
