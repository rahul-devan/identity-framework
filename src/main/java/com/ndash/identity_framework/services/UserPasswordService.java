package com.ndash.identity_framework.services;

import com.ndash.identity_framework.dto.ResetPasswordRequest;
import com.ndash.identity_framework.exception.ApiException;
import org.springframework.security.oauth2.jwt.Jwt;

public interface UserPasswordService {

    void resetPassword(final Long userId, final ResetPasswordRequest request, final Jwt jwt) throws ApiException;
}
