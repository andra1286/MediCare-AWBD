package com.medicare.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateUserRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(max = 120)
    private String fullName;

    @NotBlank
    @Size(min = 4, max = 100)
    private String password;

    @NotBlank
    private List<String> roles;
}
