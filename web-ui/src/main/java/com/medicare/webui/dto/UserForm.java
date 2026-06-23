package com.medicare.webui.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserForm {

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(max = 120)
    private String fullName;

    @Size(min = 4, max = 100)
    private String password;

    private boolean enabled = true;

    private List<String> roles = new ArrayList<>();
}
