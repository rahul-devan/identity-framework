package com.ndash.identity_framework.services;

import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.LoginRequest;
import com.ndash.identity_framework.dto.UserDto;
import org.springframework.security.oauth2.jwt.Jwt;

public interface AuthService {

    ApiResponse<UserDto> authenticate(Jwt jwt);
    ApiResponse<UserDto> authenticate(LoginRequest loginRequest);
}
