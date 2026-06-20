package com.medicare.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The data carried inside a JWT: who the user is and what they may do.
 * Lombok's @Data generates getters/setters/equals/hashCode/toString.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtClaims {
    private String username;
    private List<String> roles;
    private Long userId;
}
