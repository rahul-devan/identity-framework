package com.ndash.identity_framework.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    private String oldPassword;
    private String newPassword;

}
