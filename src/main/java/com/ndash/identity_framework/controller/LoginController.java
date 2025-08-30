package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.domain.User;
import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.LoginRequest;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.repositories.UserRepository;
import com.ndash.identity_framework.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/login")
@CrossOrigin("*")
public class LoginController {


    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }


}
