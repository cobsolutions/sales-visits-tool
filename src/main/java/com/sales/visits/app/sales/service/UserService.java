package com.sales.visits.app.sales.service;

import com.sales.visits.app.sales.dto.CreateUserRequest;
import com.sales.visits.app.sales.dto.TeamLeaderResponse;
import com.sales.visits.app.sales.dto.UserResponse;
import com.sales.visits.app.sales.exception.PermissionNotFound;
import com.sales.visits.app.sales.exception.UserNotFound;
import com.sales.visits.app.sales.model.entity.Permission;
import com.sales.visits.app.sales.model.entity.User;
import com.sales.visits.app.sales.model.enums.UserRole;
import com.sales.visits.app.sales.model.enums.UserStatus;
import com.sales.visits.app.sales.repository.PermissionRepository;
import com.sales.visits.app.sales.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PermissionRepository permissionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest createUserRequest, User superAdmin) {
        validateTeamLeader(createUserRequest);

        Set<Permission> permissions = (createUserRequest.getPermissionCodes() == null
                ? Set.<String>of()
                : createUserRequest.getPermissionCodes())
                .stream()
                .map(code -> permissionRepository.findByCode(code)
                        .orElseThrow(() -> new PermissionNotFound("Unknown permission: " + code)))
                .collect(Collectors.toSet());

        User teamLeader = null;

        if (createUserRequest.getRole() == UserRole.SALES_REP) {
            teamLeader = userRepository.findById(createUserRequest.getTeamLeaderId())
                    .orElseThrow(() -> new IllegalArgumentException("Team leader not found"));
        }

        User user = User.builder()
                .username(createUserRequest.getUsername())
                .email(createUserRequest.getEmail())
                .passwordHash(passwordEncoder.encode(createUserRequest.getPassword()))
                .role(createUserRequest.getRole())
                .createdBy(superAdmin)
                .status(UserStatus.ACTIVE)
                .permissions(permissions)
                .teamLeader(teamLeader)
                .build();

        userRepository.save(user);
        return UserResponse.from(user);
    }

    @Transactional
    public List<UserResponse> listUsers(User admin){
        return userRepository.findAll()
                .stream()
                .map(user -> UserResponse.from(user))
                .collect(Collectors.toList());
    }

    public List<TeamLeaderResponse> getTeamLeaders() {
        return userRepository.findByRole(UserRole.TEAM_LEADER)
                .stream()
                .map(TeamLeaderResponse::from)
                .collect(Collectors.toList());
    }

    private void validateTeamLeader(CreateUserRequest request) {
        if (request.getRole() == UserRole.TEAM_LEADER) {
            if (request.getTeamLeaderId() != null) {
                throw new IllegalArgumentException("TEAM_LEADER cannot have a team leader");
            }
            return;
        }

        if (request.getRole() == UserRole.SALES_REP) {
            if (request.getTeamLeaderId() == null) {
                throw new IllegalArgumentException("SALES_REP must have a team leader");
            }

            User teamLeader = userRepository.findById(request.getTeamLeaderId())
                    .orElseThrow(() -> new IllegalArgumentException("Team leader not found"));

            if (teamLeader.getRole() != UserRole.TEAM_LEADER) {
                throw new IllegalArgumentException("Assigned user must have TEAM_LEADER role");
            }
        }
    }

    @Transactional
    public void suspendUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if(user.getStatus().equals(UserStatus.ACTIVE)){
            user.setStatus(UserStatus.SUSPENDED);
            userRepository.save(user);
        }
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("SUPER_ADMIN accounts cannot be deleted.");
        }

        userRepository.delete(user);
    }
}
