package com.ndash.identity_framework.services.impl;

import com.ndash.identity_framework.domain.User;
import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.LoginRequest;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.mapper.UserMapper;
import com.ndash.identity_framework.repositories.UserRepository;
import com.ndash.identity_framework.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<UserDto> authenticate(final Jwt jwt) {
        final String azureId = jwt.getClaimAsString("oid");

        final User user = userRepository.findByAzureId(azureId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not registered in local system"
                ));

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is deactivated");
        }

        final UserDto dto = UserMapper.toDto(user);
        return ApiResponse.success(dto, HttpStatus.OK.value());
    }

    @Override
    public ApiResponse<UserDto> authenticate(final LoginRequest loginRequest) {
        final Optional<User> userOpt = userRepository.findByEmail(loginRequest.getUsername());

        if (userOpt.isEmpty() || !verifyAndUpgradePassword(userOpt.get(), loginRequest.getPassword())) {
            return ApiResponse.error("Invalid username or password", 401);
        }

        final User user = userOpt.get();
        final UserDto userDto = UserMapper.toDto(user);
        return ApiResponse.success(userDto, 200);
    }

    private boolean verifyAndUpgradePassword(final User user, final String rawPassword) {
        final String storedPassword = user.getPassword();
        if (storedPassword == null || rawPassword == null) {
            return false;
        }

        if (passwordEncoder.matches(rawPassword, storedPassword)) {
            return true;
        }

        if (storedPassword.equals(rawPassword)) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
            log.info("[identity-framework] - AUTH: upgraded legacy plaintext password for userId={}", user.getId());
            return true;
        }

        return false;
    }
}
