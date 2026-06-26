package com.sales.visits.app.sales.dto;

import com.sales.visits.app.sales.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String email;
    private UserRole role;
    private Set<String> permissions;
}
