package com.ndash.identity_framework.services;

import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.exception.ApiException;

public interface UserCommandService {

    UserDto createUser(final UserDto userDto, final Long loggedInUserId) throws ApiException;

    UserDto updateUser(final Long userId, final UserDto userDto) throws ApiException;

    void deleteUser(final Long id);
}
