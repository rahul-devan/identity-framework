package com.ndash.identity_framework.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
