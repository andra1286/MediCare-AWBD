package com.medicare.webui.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String tokenType;
    private Long userId;
    private String username;
}
