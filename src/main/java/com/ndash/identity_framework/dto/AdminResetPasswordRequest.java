package com.ndash.identity_framework.dto;

import lombok.Data;

@Data
public class AdminResetPasswordRequest {

    private Long userId;
    private String newPassword;
}
