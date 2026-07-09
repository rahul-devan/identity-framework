package com.ndash.identity_framework.controller;

import com.ndash.identity_framework.config.ApiPaths;
import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.LoginRequest;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({ApiPaths.V1 + "/login", ApiPaths.LEGACY + "/login"})
@Profile("local")
@RequiredArgsConstructor
public class LoginController {

    private final AuthService authService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> login(@Valid @RequestBody final LoginRequest request) {
        final ApiResponse<UserDto> response = authService.authenticate(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
