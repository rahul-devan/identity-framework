package com.ndash.identity_framework.services;

import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.exception.ApiException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto) throws ApiException;
    List<UserDto> getAllUsers() throws ApiException;
    UserDto getUserById(Long id);
    void syncUsersFromAzure();
    void deleteUser(Long id);
    Page<UserDto> searchUsersByUsername(String username, int page, int size);
}
