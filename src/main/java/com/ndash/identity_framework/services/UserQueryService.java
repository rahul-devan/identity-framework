package com.ndash.identity_framework.services;

import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.exception.ApiException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserQueryService {

    List<UserDto> getAllUsers() throws ApiException;

    UserDto getUserById(final Long id);

    Page<UserDto> searchUsersByUsername(final String username, final int page, final int size);

    List<UserDto> getUsersByDepartment(final Long departmentId) throws ApiException;

    List<UserDto> getAllManagers() throws ApiException;
}
