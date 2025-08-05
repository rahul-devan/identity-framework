package com.ndash.identity_framework.services.impl;

import com.ndash.identity_framework.domain.Role;
import com.ndash.identity_framework.domain.User;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.mapper.UserMapper;
import com.ndash.identity_framework.repositories.RoleRepository;
import com.ndash.identity_framework.repositories.UserRepository;
import com.ndash.identity_framework.services.AzureADService;
import com.ndash.identity_framework.services.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AzureADService azureADService;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, AzureADService azureADService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.azureADService = azureADService;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        // 1. Check local DB
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists in local database");
        }

        // 2. Check Azure AD
        com.microsoft.graph.models.User existingAzureUser = azureADService.getUserByEmail(userDto.getEmail());
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

            // Assign roles (from request or default)
            Set<Role> roles = userDto.getRoles().stream()
                    .map(name -> roleRepository.findByName(name))
                    .collect(Collectors.toSet());
            user.setRoles(roles);

            User savedUser = userRepository.save(user);
            return UserMapper.toDto(savedUser);
        }

        // 3. Create new user in Azure AD
        com.microsoft.graph.models.User azureUser =
                azureADService.createUser(userDto.getFirstName() + " " + userDto.getLastName(),
                        userDto.getEmail());

        if (azureUser == null || azureUser.id == null) {
            throw new RuntimeException("Failed to create user in Azure AD");
        }

        // 4. Assign roles
        Set<Role> roles = userDto.getRoles().stream()
                .map(name -> roleRepository.findByName(name))
                .collect(Collectors.toSet());

        // 5. Convert DTO -> Entity and set Azure AD ID
        User user = UserMapper.toEntity(userDto, roles);
        user.setAzureId(azureUser.id);
        user.setUsername(azureUser.userPrincipalName);
        user.setActive(true);

        // 6. Save to DB
        User savedUser = userRepository.save(user);

        return UserMapper.toDto(savedUser);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findByActiveTrue()
                .stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toDto)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


    @Override
    public void syncUsersFromAzure() {
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
                userRepository.save(newUser);
            } else {
                // Existing user → update + ensure active
                existingUser.setUsername(azureUser.userPrincipalName);
                existingUser.setFirstName(getFirstName(azureUser.displayName));
                existingUser.setLastName(getLastName(azureUser.displayName));
                existingUser.setPhoneNumber(azureUser.mobilePhone);
                existingUser.setEmail(azureUser.mail);
                existingUser.setActive(true);
                userRepository.save(existingUser);
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
            Role role = roleRepository.findByName(group.displayName);
            if (role == null) {
                Role newRole = new Role();
                newRole.setName(group.displayName);
                roleRepository.save(newRole);
            }
        }
    }

}
