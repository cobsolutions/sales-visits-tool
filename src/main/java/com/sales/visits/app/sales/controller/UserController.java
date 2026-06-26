package com.sales.visits.app.sales.controller;

import com.sales.visits.app.sales.dto.LoginRequest;
import com.sales.visits.app.sales.dto.LoginResponse;
import com.sales.visits.app.sales.model.entity.Permission;
import com.sales.visits.app.sales.model.entity.User;
import com.sales.visits.app.sales.repository.UserRepository;
import com.sales.visits.app.sales.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;


    public UserController(AuthenticationManager authenticationManager, UserRepository userRepository, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        } catch (BadCredentialsException | DisabledException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow();

        String token = jwtService.generateToken(user);

        LoginResponse body = LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .permissions(user.getPermissions().stream()
                        .map(Permission::getCode)
                        .collect(Collectors.toSet()))
                .build();

        return ResponseEntity.ok(body);
    }
}
