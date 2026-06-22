package com.medicare.webui.security;

import com.medicare.common.dto.JwtClaims;
import com.medicare.common.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/** Stores the JWT in an HttpOnly cookie for remember-me. */
@Component
@RequiredArgsConstructor
public class JwtRememberMeServices implements RememberMeServices {

    static final String COOKIE_NAME = "MEDICARE_REMEMBER_JWT";

    private final JwtUtil jwtUtil;

    @Override
    public Authentication autoLogin(HttpServletRequest request, HttpServletResponse response) {
        String jwt = readCookie(request, COOKIE_NAME);
        if (jwt == null || !jwtUtil.isValid(jwt)) {
            return null;
        }
        JwtClaims claims = jwtUtil.parseToken(jwt);
        List<SimpleGrantedAuthority> authorities = claims.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        return JwtAuthenticationSupport.authenticated(claims.getUsername(), jwt, authorities);
    }

    @Override
    public void loginFail(HttpServletRequest request, HttpServletResponse response) {
        clearCookie(response);
    }

    @Override
    public void loginSuccess(HttpServletRequest request, HttpServletResponse response,
                             Authentication successfulAuthentication) {
        if (request.getParameter("remember-me") == null) {
            return;
        }
        String jwt = JwtAuthenticationSupport.resolveJwt(successfulAuthentication);
        if (jwt == null) {
            return;
        }
        Cookie cookie = new Cookie(COOKIE_NAME, jwt);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);
    }

    public static void logout(HttpServletResponse response) {
        clearCookie(response);
    }

    private static String readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private static void clearCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
