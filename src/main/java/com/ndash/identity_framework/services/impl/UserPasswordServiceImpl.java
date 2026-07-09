package com.ndash.identity_framework.services.impl;

import com.ndash.identity_framework.domain.User;
import com.ndash.identity_framework.dto.ResetPasswordRequest;
import com.ndash.identity_framework.exception.ApiException;
import com.ndash.identity_framework.repositories.UserRepository;
import com.ndash.identity_framework.security.RoleConstants;
import com.ndash.identity_framework.services.UserPasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserPasswordServiceImpl implements UserPasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void resetPassword(final Long userId, final ResetPasswordRequest request, final Jwt jwt)
            throws ApiException {
        try {
            final User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            final String loggedInEmail = jwt.getSubject();

            final User loggedInUser = userRepository.findByEmail(loggedInEmail)
                    .orElseThrow(() -> new RuntimeException("Logged in user not found"));

            final boolean isAdmin = loggedInUser.getUserRoles().stream()
                    .anyMatch(role -> role.getRole().getName().equalsIgnoreCase(RoleConstants.ADMINISTRATION)
                            || role.getRole().getName().equalsIgnoreCase(RoleConstants.SUPER_ADMIN));

            if (isAdmin) {
                if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
                    throw new RuntimeException("New password is required");
                }

                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                userRepository.save(user);

                log.info("Admin {} reset password for user {}", loggedInUser.getEmail(), user.getEmail());
                return;
            }

            if (!loggedInUser.getId().equals(userId)) {
                throw new RuntimeException("You can only change your own password");
            }

            if (request.getOldPassword() == null || request.getNewPassword() == null) {
                throw new RuntimeException("Old password and new password are required");
            }

            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new RuntimeException("Old password is incorrect");
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            log.info("User {} changed their password", user.getEmail());

        } catch (Exception ex) {
            log.error("Error resetting password", ex);
            throw new ApiException(ex.getMessage());
        }
    }
}
