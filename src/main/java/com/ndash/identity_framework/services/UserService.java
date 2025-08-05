package com.ndash.identity_framework.services;

import com.ndash.identity_framework.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);
    List<UserDto> getAllUsers();
    UserDto getUserById(Long id);
    void syncUsersFromAzure();
    void deleteUser(Long id);
}
