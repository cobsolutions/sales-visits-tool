package com.sales.visits.app.sales.controller;

import com.sales.visits.app.sales.dto.request.LoginRequest;
import com.sales.visits.app.sales.dto.response.LoginResponse;
import com.sales.visits.app.sales.dto.response.UserSummaryResponse;
import com.sales.visits.app.sales.model.entity.Permission;
import com.sales.visits.app.sales.model.entity.User;
import com.sales.visits.app.sales.repository.UserRepository;
import com.sales.visits.app.sales.security.JwtService;
import com.sales.visits.app.sales.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api")
public class UserController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtService jwtService;


    public UserController(AuthenticationManager authenticationManager, UserRepository userRepository, UserService userService, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.userService = userService;
        this.jwtService = jwtService;
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(userService.login(loginRequest));
    }

    @GetMapping("/joined-visitor")
    @PreAuthorize("hasAnyRole('TEAM_LEADER','SALES_REP')")
    public List<UserSummaryResponse> joinedVisitors(){
        return userService.findJoinedVisitors().stream()
                .map(u->new UserSummaryResponse(u.getUsername(),u.getRole())).toList();
    }
}
