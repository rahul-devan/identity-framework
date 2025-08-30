package com.ndash.identity_framework.services.impl;

import com.ndash.identity_framework.domain.Role;
import com.ndash.identity_framework.domain.User;
import com.ndash.identity_framework.domain.UserRole;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.exception.ApiException;
import com.ndash.identity_framework.helper.AzureUserUpdater;
import com.ndash.identity_framework.mapper.UserMapper;
import com.ndash.identity_framework.repositories.RoleRepository;
import com.ndash.identity_framework.repositories.UserRepository;
import com.ndash.identity_framework.services.AzureADService;
import com.ndash.identity_framework.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AzureADService azureADService;
    private final AzureUserUpdater azureUserUpdater;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, AzureADService azureADService, AzureUserUpdater azureUserUpdater) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.azureADService = azureADService;
        this.azureUserUpdater = azureUserUpdater;
    }

    @Override
    public UserDto createUser(UserDto userDto) throws ApiException {

        Role defaultRole = roleRepository.findByName("user").orElse(null); // Need to change this logic later
        if (defaultRole == null) {
            throw new RuntimeException("Default role 'user' not found in DB");
        }

        try {
            // 1. Check local DB
            if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
                log.warn("User already exists in local database");
                throw new RuntimeException("User already exists in local database");
            }

            // 2. Check Azure AD
            com.microsoft.graph.models.User existingAzureUser =
                    azureADService.getUserByEmail(userDto.getEmail());

            if (existingAzureUser != null) {
                // Option A: Sync them into local DB
                User user = new User();
                user.setAzureId(existingAzureUser.id);
                user.setUsername(existingAzureUser.userPrincipalName);
                user.setEmail(existingAzureUser.mail);
                user.setFirstName(getFirstName(existingAzureUser.displayName));
                user.setLastName(getLastName(existingAzureUser.displayName));
                user.setPhoneNumber(existingAzureUser.mobilePhone);
                user.setActive(true);
                user.setPassword("Test123");

                // Assign default role
                UserRole userRole = new UserRole();
                userRole.setUser(user);
                userRole.setRole(defaultRole);
                user.setUserRoles(Set.of(userRole));

                User savedUser = userRepository.save(user);
                log.info("User already present in azure, synced to database");
                return UserMapper.toDto(savedUser);
            }

            // 3. Create new user in Azure AD
            com.microsoft.graph.models.User azureUser =
                    azureADService.createUser(userDto.getFirstName(), userDto.getEmail());

            if (azureUser == null || azureUser.id == null) {
                throw new RuntimeException("Failed to create user in Azure AD");
            }
            log.info("User created in Azure AD");

            // 5. Resolve roles
            Set<Role> assignedRoles;
            if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
                assignedRoles = userDto.getRoles().stream()
                        .map(roleName -> roleRepository.findByName(roleName).orElse(defaultRole))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                log.info("Assigned roles: {}", assignedRoles);
            } else {
                assignedRoles = Set.of(defaultRole);
                log.info("Assigned default role only");
            }
            // 4. Convert DTO -> Entity
            User user = UserMapper.toEntity(userDto, assignedRoles);
            user.setAzureId(azureUser.id);
            user.setUsername(azureUser.userPrincipalName);
            user.setActive(true);
            user.setPassword("Test123");

            // 7. Save to DB
            User savedUser = userRepository.save(user);
            log.info("User added to database with username: {}", savedUser.getUsername());

            return UserMapper.toDto(savedUser);

        } catch (Exception ex) {
            log.error("Exception occurred while creating user: {}", ex.getMessage(), ex);
            throw new ApiException(ex.getMessage());
        }
    }

    @Override
    public List<UserDto> getAllUsers() throws ApiException {
        try {
            List<User> users = userRepository.findByActiveTrue();
            log.info("Fetched all users, total size: {}", users.size());
            return users.stream()
                    .map(UserMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception ex){
            log.error("Exception occurred while fetching users: {}", ex.getMessage());
            throw new ApiException(ex.getMessage());
        }
    }

    @Override
    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toDto)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


    @Override
    public void syncUsersFromAzure() {
        Set<Role> assignedRoles = new HashSet<>();
        Role defaultRole = roleRepository.findByName("user").orElse(null);// Need to change this logic later
        assignedRoles.add(defaultRole);
        // 1. Fetch Azure users
        List<com.microsoft.graph.models.User> azureUsers = azureADService.getAllUsers();
        Set<String> azureUserIds = azureUsers.stream()
                .map(u -> u.id)
                .collect(Collectors.toSet());

        // 2. Mark inactive users (local but not in Azure)
        List<User> localUsers = userRepository.findAll();
        for (User localUser : localUsers) {
            if (!azureUserIds.contains(localUser.getAzureId())) {
                localUser.setActive(false); // Soft delete
                userRepository.save(localUser);
            }
        }

        // 3. Insert or update active users from Azure
        for (com.microsoft.graph.models.User azureUser : azureUsers) {
            if (azureUser.mail == null) continue;

            User existingUser = userRepository.findByAzureId(azureUser.id)
                    .orElseGet(() -> userRepository.findByEmail(azureUser.mail).orElse(null));

            if (existingUser == null) {
                // New user → insert
                User newUser = new User();
                newUser.setAzureId(azureUser.id);
                newUser.setUsername(azureUser.userPrincipalName);
                newUser.setEmail(azureUser.mail);
                newUser.setFirstName(getFirstName(azureUser.displayName));
                newUser.setLastName(getLastName(azureUser.displayName));
                newUser.setPhoneNumber(azureUser.mobilePhone);
                newUser.setActive(true);
                Set<UserRole> userRoles = assignedRoles.stream()
                        .map(role -> {
                            UserRole ur = new UserRole();
                            ur.setUser(newUser);
                            ur.setRole(role);
                            return ur;
                        })
                        .collect(Collectors.toSet());
                newUser.setUserRoles(userRoles);
                userRepository.save(newUser);
            } else {
                // Existing user → update + ensure active
                if (azureUserUpdater.updateUserFromAzure(existingUser, azureUser, assignedRoles)) {
                    userRepository.save(existingUser);
                }
            }
        }

        // 4. Sync roles (optional)
        syncRolesFromAzure();
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Delete from Azure AD
        try {
            azureADService.deleteUser(user.getUsername());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to delete user from Azure AD: " + ex.getMessage());
        }

        // 2. Delete from local DB
        userRepository.delete(user);
    }

    @Override
    public Page<UserDto> searchUsersByUsername(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());

        Page<User> userPage = userRepository.findByUsernameContainingIgnoreCaseAndActiveTrue(username, pageable);

        return userPage.map(UserMapper::toDto);  // Converts each User to UserDto
    }


    private String getFirstName(String displayName) {
        if (displayName == null) return "";
        return displayName.split(" ")[0];
    }

    private String getLastName(String displayName) {
        if (displayName == null || !displayName.contains(" ")) return "";
        return displayName.substring(displayName.indexOf(" ") + 1);
    }

    private void syncRolesFromAzure() {
        List<com.microsoft.graph.models.Group> azureGroups = azureADService.getAllGroups();

        for (com.microsoft.graph.models.Group group : azureGroups) {
            // Ensure role exists locally
            Role role = roleRepository.findByName(group.displayName).orElse(null);
            if (role == null) {
                Role newRole = new Role();
                newRole.setName(group.displayName);
                roleRepository.save(newRole);
            }
        }
    }

}
