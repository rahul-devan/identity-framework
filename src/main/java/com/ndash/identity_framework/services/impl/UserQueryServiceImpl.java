package com.ndash.identity_framework.services.impl;

import com.ndash.identity_framework.domain.User;
import com.ndash.identity_framework.dto.SimpleUserDto;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.exception.ApiException;
import com.ndash.identity_framework.mapper.UserMapper;
import com.ndash.identity_framework.repositories.UserApplicationRepository;
import com.ndash.identity_framework.repositories.UserRepository;
import com.ndash.identity_framework.services.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;
    private final UserApplicationRepository userApplicationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() throws ApiException {
        try {
            final List<User> users = userRepository.findAll();
            log.info("Fetched all users, total size: {}", users.size());

            return users.stream()
                    .filter(User::isActive)
                    .map(user -> {
                        final UserDto dto = UserMapper.toDto(user);
                        dto.setSubordinates(getSubordinates(user.getId()));
                        return dto;
                    })
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            log.error("Exception occurred while fetching users: {}", ex.getMessage());
            throw new ApiException(ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(final Long id) {
        try {
            final User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            final UserDto dto = UserMapper.toDto(user);
            dto.setSubordinates(getSubordinates(user.getId()));
            dto.setApplications(
                    userApplicationRepository.findByUserIdAndActiveTrue(user.getId())
                            .stream()
                            .map(UserMapper::toUserApplicationDto)
                            .toList()
            );

            return dto;
        } catch (Exception ex) {
            log.error("Exception occurred while fetching user by id: {}", ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> searchUsersByUsername(final String username, final int page, final int size) {
        final Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());

        final Page<User> userPage =
                userRepository.findByUsernameContainingIgnoreCaseAndActiveTrue(username, pageable);

        return userPage.map(user -> {
            final UserDto dto = UserMapper.toDto(user);
            dto.setSubordinates(getSubordinates(user.getId()));
            return dto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByDepartment(final Long departmentId) throws ApiException {
        try {
            final List<User> users = userRepository.findByDepartmentIdAndActiveTrue(departmentId);
            log.info("Fetched users for department {}, total size: {}", departmentId, users.size());
            return users.stream()
                    .map(UserMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.error("Exception occurred while fetching users by department: {}", ex.getMessage());
            throw new ApiException(ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllManagers() throws ApiException {
        try {
            final List<User> managers = userRepository.findAllManagers();

            return managers.stream()
                    .map(UserMapper::toDto)
                    .toList();

        } catch (Exception ex) {
            log.error("Exception occurred while fetching managers: {}", ex.getMessage(), ex);
            throw new ApiException("Failed to fetch manager users");
        }
    }

    private Set<SimpleUserDto> getSubordinates(final Long userId) {
        return userRepository.findByManagerId(userId).stream()
                .filter(User::isActive)
                .map(u -> {
                    final SimpleUserDto dto = new SimpleUserDto();
                    dto.setId(u.getId());
                    dto.setFirstName(u.getFirstName());
                    dto.setLastName(u.getLastName());
                    dto.setEmail(u.getEmail());
                    return dto;
                })
                .collect(Collectors.toSet());
    }
}
