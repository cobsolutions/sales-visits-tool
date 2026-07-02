package com.sales.visits.app.sales.controller;

import com.sales.visits.app.sales.dto.request.CreateUserRequest;
import com.sales.visits.app.sales.dto.response.TeamLeaderResponse;
import com.sales.visits.app.sales.dto.response.UserResponse;
import com.sales.visits.app.sales.model.entity.User;
import com.sales.visits.app.sales.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class SuperAdminController {
    private final UserService userService;

    public SuperAdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/createUser")
    @PreAuthorize("hasRole('SUPER_ADMIN') and " +
            "hasAuthority('USER_MANAGE') and " +
            "hasAuthority('PERMISSION_ASSIGN')")
    public UserResponse createUser(@RequestBody CreateUserRequest createUserRequest, @AuthenticationPrincipal User superAdmin) {
        log.info("CONTROLLER METHOD REACHED. Principal: " + superAdmin);
        return userService.createUser(createUserRequest, superAdmin);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('SUPER_ADMIN') and " +
            "hasAuthority('USER_MANAGE')")
    public List<UserResponse> getUsers(@AuthenticationPrincipal User superAdmin) {
        return userService.listUsers(superAdmin);
    }

    @GetMapping("/team-leaders")
    @PreAuthorize("hasRole('SUPER_ADMIN') and " +
            "hasAuthority('USER_MANAGE')")
    public List<TeamLeaderResponse> getTeamLeaders(@AuthenticationPrincipal User superAdmin) {
        return userService.getTeamLeaders();
    }

    @PatchMapping("/users/{id}/suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN') and " +
            "hasAuthority('USER_MANAGE')")
    public ResponseEntity<?> suspendUser(@PathVariable Long id, @AuthenticationPrincipal User superAdmin) {
        userService.suspendUser(id, superAdmin);
        return ResponseEntity.ok(Map.of("message", "User suspended"));
    }

    @PatchMapping("/users/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN') and " +
            "hasAuthority('USER_MANAGE')")
    public ResponseEntity<?> activateUser(@PathVariable Long id, @AuthenticationPrincipal User superAdmin) {
        userService.activateUser(id, superAdmin);
        return ResponseEntity.ok(Map.of("message", "User activated"));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') and " +
            "hasAuthority('USER_MANAGE')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @AuthenticationPrincipal User superAdmin) {
        userService.deleteUser(id, superAdmin);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }
}
