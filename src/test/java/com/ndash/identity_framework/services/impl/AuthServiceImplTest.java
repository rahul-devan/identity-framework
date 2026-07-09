package com.ndash.identity_framework.services.impl;

import com.ndash.identity_framework.domain.User;
import com.ndash.identity_framework.dto.ApiResponse;
import com.ndash.identity_framework.dto.LoginRequest;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void authenticate_withBcryptPassword_ok() {
        // given
        final User user = buildUser(1L, "$2a$10$encoded");
        final LoginRequest request = new LoginRequest();
        request.setUsername("user@example.com");
        request.setPassword("secret");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "$2a$10$encoded")).thenReturn(true);

        // when
        final ApiResponse<UserDto> response = authService.authenticate(request);

        // then
        assertEquals("SUCCESS", response.getStatusMessage());
        assertEquals(200, response.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticate_withLegacyPlaintextPassword_upgradesAndSucceeds() {
        // given
        final User user = buildUser(2L, "legacy-plain");
        final LoginRequest request = new LoginRequest();
        request.setUsername("user@example.com");
        request.setPassword("legacy-plain");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("legacy-plain", "legacy-plain")).thenReturn(false);
        when(passwordEncoder.encode("legacy-plain")).thenReturn("$2a$10$upgraded");

        // when
        final ApiResponse<UserDto> response = authService.authenticate(request);

        // then
        assertEquals("SUCCESS", response.getStatusMessage());
        final ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("$2a$10$upgraded", savedUser.getValue().getPassword());
    }

    @Test
    void authenticate_withInvalidPassword_ko() {
        // given
        final User user = buildUser(3L, "$2a$10$encoded");
        final LoginRequest request = new LoginRequest();
        request.setUsername("user@example.com");
        request.setPassword("wrong");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("wrong"), any())).thenReturn(false);

        // when
        final ApiResponse<UserDto> response = authService.authenticate(request);

        // then
        assertEquals("ERROR", response.getStatusMessage());
        assertEquals(401, response.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    private static User buildUser(final Long id, final String password) {
        final User user = new User();
        user.setId(id);
        user.setEmail("user@example.com");
        user.setUsername("user@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword(password);
        user.setActive(true);
        user.setUserRoles(new HashSet<>());
        return user;
    }
}
