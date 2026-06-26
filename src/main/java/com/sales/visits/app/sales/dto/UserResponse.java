package com.sales.visits.app.sales.dto;

import com.sales.visits.app.sales.model.entity.Permission;
import com.sales.visits.app.sales.model.entity.User;
import com.sales.visits.app.sales.model.enums.UserRole;
import com.sales.visits.app.sales.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private UserStatus status;

    private Long teamLeaderId;
    private String teamLeaderUsername;

    private Long createdById;
    private String createdByUsername;

    private Set<String> permissionCodes;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .teamLeaderId(user.getTeamLeader() != null ? user.getTeamLeader().getId() : null)
                .teamLeaderUsername(user.getTeamLeader() != null ? user.getTeamLeader().getUsername() : null)
                .createdById(user.getCreatedBy() != null ? user.getCreatedBy().getId() : null)
                .createdByUsername(user.getCreatedBy() != null ? user.getCreatedBy().getUsername() : null)
                .permissionCodes(user.getPermissions().stream()
                        .map(Permission::getCode)
                        .collect(Collectors.toSet()))
                .build();
    }
}
