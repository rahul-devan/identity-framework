package com.ndash.identity_framework.services.impl;

import com.ndash.identity_framework.dto.ResetPasswordRequest;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.exception.ApiException;
import com.ndash.identity_framework.services.AzureSyncService;
import com.ndash.identity_framework.services.UserCommandService;
import com.ndash.identity_framework.services.UserPasswordService;
import com.ndash.identity_framework.services.UserQueryService;
import com.ndash.identity_framework.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserQueryService userQueryService;
    private final UserPasswordService userPasswordService;
    private final AzureSyncService azureSyncService;
    private final UserCommandService userCommandService;

    @Override
    public UserDto createUser(final UserDto userDto, final Long loggedInUserId) throws ApiException {
        return userCommandService.createUser(userDto, loggedInUserId);
    }

    @Override
    public List<UserDto> getAllUsers() throws ApiException {
        return userQueryService.getAllUsers();
    }

    @Override
    public UserDto getUserById(final Long id) {
        return userQueryService.getUserById(id);
    }

    @Override
    public void syncUsersFromAzure() {
        azureSyncService.syncUsersFromAzure();
    }

    @Override
    public void deleteUser(final Long id) {
        userCommandService.deleteUser(id);
    }

    @Override
    public Page<UserDto> searchUsersByUsername(final String username, final int page, final int size) {
        return userQueryService.searchUsersByUsername(username, page, size);
    }

    @Override
    public UserDto updateUser(final Long userId, final UserDto userDto) throws ApiException {
        return userCommandService.updateUser(userId, userDto);
    }

    @Override
    public void resetPassword(final Long userId, final ResetPasswordRequest request, final Jwt jwt)
            throws ApiException {
        userPasswordService.resetPassword(userId, request, jwt);
    }

    @Override
    public List<UserDto> getUsersByDepartment(final Long departmentId) throws ApiException {
        return userQueryService.getUsersByDepartment(departmentId);
    }

    @Override
    public List<UserDto> getAllManagers() throws ApiException {
        return userQueryService.getAllManagers();
    }
}
