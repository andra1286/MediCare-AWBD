package com.medicare.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRequest {

    @Email
    private String email;

    @Size(max = 120)
    private String fullName;

    @Size(min = 4, max = 100)
    private String password;

    private Boolean enabled;

    private List<String> roles;
}
