package com.medicare.webui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Web UI security (mandatory Spring Security requirement):
 * custom login page, 3 roles, BCrypt, remember-me, CSRF (default on), logout, role-based access.
 * <p>
 * DEV NOTE: users are in-memory here so the UI runs standalone. Dev A replaces this with
 * authentication against identity-service (JDBC + JWT) without changing the templates.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Three demo users, one per role, with BCrypt-hashed passwords. */
    @Bean
    public InMemoryUserDetailsManager userDetailsManager(PasswordEncoder encoder) {
        UserDetails admin = User.withUsername("admin")
                .password(encoder.encode("admin")).roles("ADMIN").build();
        UserDetails doctor = User.withUsername("doctor")
                .password(encoder.encode("doctor")).roles("DOCTOR").build();
        UserDetails patient = User.withUsername("patient")
                .password(encoder.encode("patient")).roles("PATIENT").build();
        return new InMemoryUserDetailsManager(admin, doctor, patient);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**", "/webjars/**", "/error").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        // Only ADMIN may change the medication catalog (create/edit/update/delete).
                        // Viewing the catalog list stays available to any authenticated user.
                        .requestMatchers(HttpMethod.GET, "/medications/new", "/medications/*/edit")
                            .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/medications", "/medications/**")
                            .hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .rememberMe(rm -> rm.key("medicare-remember-me-key").tokenValiditySeconds(86400));
        // Access-denied (403) and not-found (404) flow to the single generic error.html.
        return http.build();
    }
}
