package com.sales.visits.app.sales.controller;

import com.sales.visits.app.sales.dto.request.LoginRequest;
import com.sales.visits.app.sales.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(userService.login(loginRequest));
    }

}
