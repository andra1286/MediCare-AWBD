package com.medicare.identity.service;

import com.medicare.common.exception.DuplicateResourceException;
import com.medicare.common.security.JwtUtil;
import com.medicare.identity.domain.Role;
import com.medicare.identity.domain.User;
import com.medicare.identity.dto.LoginRequest;
import com.medicare.identity.dto.RegisterRequest;
import com.medicare.identity.repository.PatientProfileRepository;
import com.medicare.identity.repository.RoleRepository;
import com.medicare.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PatientProfileRepository patientProfileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginReturnsTokenForValidCredentials() {
        User user = User.builder()
                .id(1L)
                .username("patient")
                .passwordHash("hash")
                .enabled(true)
                .roles(Set.of(Role.builder().name("ROLE_PATIENT").build()))
                .build();
        when(userRepository.findByUsername("patient")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("patient", "hash")).thenReturn(true);
        when(jwtUtil.generateToken(any())).thenReturn("jwt-token");

        var response = authService.login(new LoginRequest() {{
            setUsername("patient");
            setPassword("patient");
        }});

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("patient");
    }

    @Test
    void loginRejectsInvalidPassword() {
        User user = User.builder().username("x").passwordHash("hash").enabled(true).build();
        when(userRepository.findByUsername("x")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest() {{
            setUsername("x");
            setPassword("wrong");
        }})).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void registerCreatesPatientUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newbie");
        request.setEmail("newbie@test.local");
        request.setFullName("New Patient");
        request.setPassword("secret");

        when(userRepository.existsByUsername("newbie")).thenReturn(false);
        when(userRepository.existsByEmail("newbie@test.local")).thenReturn(false);
        when(roleRepository.findByName("ROLE_PATIENT"))
                .thenReturn(Optional.of(Role.builder().name("ROLE_PATIENT").build()));
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });
        when(patientProfileRepository.existsByPersonalId("PAT-10")).thenReturn(false);
        when(jwtUtil.generateToken(any())).thenReturn("token");

        var response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("token");
        verify(patientProfileRepository).save(any());
    }

    @Test
    void registerRejectsDuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("taken");
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
