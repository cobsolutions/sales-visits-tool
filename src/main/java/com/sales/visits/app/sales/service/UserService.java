package com.sales.visits.app.sales.service;

import com.sales.visits.app.sales.dto.request.CreateUserRequest;
import com.sales.visits.app.sales.dto.request.LoginRequest;
import com.sales.visits.app.sales.dto.response.LoginResponse;
import com.sales.visits.app.sales.dto.response.TeamLeaderResponse;
import com.sales.visits.app.sales.dto.response.UserResponse;
import com.sales.visits.app.sales.exception.AccountSuspendedException;
import com.sales.visits.app.sales.exception.InvalidCredentialsException;
import com.sales.visits.app.sales.exception.PermissionNotFound;
import com.sales.visits.app.sales.model.entity.Permission;
import com.sales.visits.app.sales.model.entity.User;
import com.sales.visits.app.sales.model.enums.UserRole;
import com.sales.visits.app.sales.model.enums.UserStatus;
import com.sales.visits.app.sales.repository.PermissionRepository;
import com.sales.visits.app.sales.repository.UserRepository;
import com.sales.visits.app.sales.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PermissionRepository permissionRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
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
                .filter(user -> !user.getId().equals(admin.getId()))
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
    public void suspendUser(Long userId, User admin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getId().equals(admin.getId())) {
            throw new IllegalArgumentException("You cannot suspend your own account");
        }

        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(Long userId, User admin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getId().equals(admin.getId())) {
            throw new IllegalArgumentException("You cannot suspend your own account");
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId, User admin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getId().equals(admin.getId())) {
            throw new IllegalArgumentException("You cannot delete your own account");
        }

        userRepository.delete(user);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()));
        } catch (DisabledException ex) {
            throw  new AccountSuspendedException("Your account has been suspended.");
        } catch (BadCredentialsException ex) {
            throw  new InvalidCredentialsException("Invalid username or password.");
        }
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow();

        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .permissions(user.getPermissions().stream()
                        .map(Permission::getCode)
                        .collect(Collectors.toSet()))
                .build();
    }

}
