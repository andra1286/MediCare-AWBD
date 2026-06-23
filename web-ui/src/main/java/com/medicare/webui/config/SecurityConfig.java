package com.medicare.webui.config;

import com.medicare.webui.security.IdentityAuthenticationProvider;
import com.medicare.webui.security.JwtRememberMeServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Web UI security: login via identity-service, role-based access, remember-me, CSRF.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final IdentityAuthenticationProvider identityAuthenticationProvider;
    private final JwtRememberMeServices jwtRememberMeServices;

    public SecurityConfig(IdentityAuthenticationProvider identityAuthenticationProvider,
                          JwtRememberMeServices jwtRememberMeServices) {
        this.identityAuthenticationProvider = identityAuthenticationProvider;
        this.jwtRememberMeServices = jwtRememberMeServices;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.authenticationProvider(identityAuthenticationProvider);
        return builder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**", "/webjars/**", "/error").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers("/doctors/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/patients/**").hasAnyRole("ADMIN", "DOCTOR")
                        .requestMatchers("/patients/**").hasRole("ADMIN")
                        .requestMatchers("/records/**").hasAnyRole("ADMIN", "DOCTOR")
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
                        .addLogoutHandler((request, response, auth) ->
                                JwtRememberMeServices.logout(response))
                        .permitAll())
                .rememberMe(rm -> rm.rememberMeServices(jwtRememberMeServices));
        return http.build();
    }
}
