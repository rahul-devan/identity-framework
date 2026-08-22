package com.ndash.identity_framework.services;

import com.ndash.identity_framework.dto.AdminResetPasswordRequest;
import com.ndash.identity_framework.dto.FetchTypeEnum;
import com.ndash.identity_framework.dto.ResetPasswordRequest;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.exception.ApiException;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto, Long loggedInUserId) throws ApiException;
    List<UserDto> getAllUsers(final FetchTypeEnum fetchTypeEnum) throws ApiException;
    UserDto getUserById(Long id, FetchTypeEnum fetchTypeEnum);
    void syncUsersFromAzure();
    void deleteUser(Long id);
    Page<UserDto> searchUsersByUsername(String username, int page, int size);

    UserDto updateUser(Long userId, UserDto userDto) throws ApiException;

    UserDto updateUserActiveness(Long userId, UserDto userDto) throws ApiException;

    void resetPassword(Long userId, ResetPasswordRequest request, Jwt jwt) throws ApiException;

    void adminResetPassword(AdminResetPasswordRequest request, Jwt jwt) throws ApiException;

    List<UserDto> getUsersByDepartment(Long departmentId) throws ApiException;
    List<UserDto> getAllManagers() throws ApiException;
}
