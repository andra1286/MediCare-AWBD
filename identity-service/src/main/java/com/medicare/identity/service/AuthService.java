package com.medicare.identity.service;

import com.medicare.common.dto.JwtClaims;
import com.medicare.common.exception.DuplicateResourceException;
import com.medicare.common.security.JwtUtil;
import com.medicare.identity.domain.PatientProfile;
import com.medicare.identity.domain.Role;
import com.medicare.identity.domain.User;
import com.medicare.identity.dto.AuthResponse;
import com.medicare.identity.dto.LoginRequest;
import com.medicare.identity.dto.RegisterRequest;
import com.medicare.identity.repository.PatientProfileRepository;
import com.medicare.identity.repository.RoleRepository;
import com.medicare.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String ROLE_PATIENT = "ROLE_PATIENT";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        if (!user.isEnabled() || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }
        Role patientRole = roleRepository.findByName(ROLE_PATIENT)
                .orElseThrow(() -> new IllegalStateException("ROLE_PATIENT not seeded"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(Set.of(patientRole))
                .build();
        user = userRepository.save(user);

        String personalId = request.getPersonalId() != null && !request.getPersonalId().isBlank()
                ? request.getPersonalId()
                : "PAT-" + user.getId();
        if (patientProfileRepository.existsByPersonalId(personalId)) {
            throw new DuplicateResourceException("Personal id already registered");
        }
        PatientProfile profile = PatientProfile.builder()
                .user(user)
                .personalId(personalId)
                .phone(request.getPhone())
                .build();
        patientProfileRepository.save(profile);

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        String token = jwtUtil.generateToken(new JwtClaims(user.getUsername(), roles, user.getId()));
        return new AuthResponse(token, "Bearer", user.getId(), user.getUsername());
    }
}
