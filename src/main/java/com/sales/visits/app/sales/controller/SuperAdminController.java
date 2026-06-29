package com.sales.visits.app.sales.controller;

import com.sales.visits.app.sales.dto.CreateUserRequest;
import com.sales.visits.app.sales.dto.TeamLeaderResponse;
import com.sales.visits.app.sales.dto.UserResponse;
import com.sales.visits.app.sales.model.entity.User;
import com.sales.visits.app.sales.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
