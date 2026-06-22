package com.medicare.identity.service;

import com.medicare.common.dto.PageResponse;
import com.medicare.common.dto.UserDto;
import com.medicare.common.exception.BusinessRuleException;
import com.medicare.common.exception.DuplicateResourceException;
import com.medicare.common.exception.ResourceNotFoundException;
import com.medicare.identity.domain.Role;
import com.medicare.identity.domain.User;
import com.medicare.identity.dto.CreateUserRequest;
import com.medicare.identity.dto.UpdateUserRequest;
import com.medicare.identity.repository.RoleRepository;
import com.medicare.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final IdentityMapper mapper;

    @Transactional(readOnly = true)
    public PageResponse<UserDto> list(Pageable pageable) {
        Page<UserDto> page = userRepository.findAll(pageable).map(mapper::toUserDto);
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        return mapper.toUserDto(findUser(id));
    }

    @Transactional
    public UserDto create(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(resolveRoles(request.getRoles()))
                .build();
        return mapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    public UserDto update(Long id, UpdateUserRequest request) {
        User user = findUser(id);
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        if (request.getRoles() != null) {
            user.setRoles(resolveRoles(request.getRoles()));
        }
        return mapper.toUserDto(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        User user = findUser(id);
        userRepository.delete(user);
    }

    User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private Set<Role> resolveRoles(java.util.List<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String name : roleNames) {
            String normalized = name.startsWith("ROLE_") ? name : "ROLE_" + name;
            Role role = roleRepository.findByName(normalized)
                    .orElseThrow(() -> new BusinessRuleException("Unknown role: " + name));
            roles.add(role);
        }
        return roles;
    }
}
