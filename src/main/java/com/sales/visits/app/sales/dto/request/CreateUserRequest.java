package com.sales.visits.app.sales.dto.request;

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
public class CreateUserRequest {
    private String username;
    private String password;
    private String email;
    private UserRole role;
    private Long teamLeaderId;
    private Set<String> permissionCodes;
}
