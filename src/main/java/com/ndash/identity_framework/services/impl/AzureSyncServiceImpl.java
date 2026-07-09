package com.ndash.identity_framework.services.impl;

import com.ndash.identity_framework.domain.Role;
import com.ndash.identity_framework.domain.User;
import com.ndash.identity_framework.domain.UserRole;
import com.ndash.identity_framework.helper.AzureUserUpdater;
import com.ndash.identity_framework.repositories.RoleRepository;
import com.ndash.identity_framework.repositories.UserRepository;
import com.ndash.identity_framework.security.RoleConstants;
import com.ndash.identity_framework.services.AzureADService;
import com.ndash.identity_framework.services.AzureSyncService;
import com.microsoft.graph.models.Group;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AzureSyncServiceImpl implements AzureSyncService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AzureADService azureADService;
    private final AzureUserUpdater azureUserUpdater;
    private final AzureSyncServiceImpl self;

    public AzureSyncServiceImpl(
            final UserRepository userRepository,
            final RoleRepository roleRepository,
            final AzureADService azureADService,
            final AzureUserUpdater azureUserUpdater,
            @Lazy final AzureSyncServiceImpl self) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.azureADService = azureADService;
        this.azureUserUpdater = azureUserUpdater;
        this.self = self;
    }

    @Override
    public void syncUsersFromAzure() {
        final Role defaultRole = roleRepository.findByName(RoleConstants.USER).orElse(null);
        final Set<Role> assignedRoles = new HashSet<>();
        assignedRoles.add(defaultRole);

        final List<com.microsoft.graph.models.User> azureUsers = azureADService.getAllUsers();
        final Set<String> azureUserIds = azureUsers.stream()
                .map(u -> u.id)
                .collect(Collectors.toSet());

        self.markInactiveUsers(azureUserIds);
        self.upsertUsersFromAzure(azureUsers, assignedRoles);
        self.syncRolesFromAzure();
    }

    @Transactional
    void markInactiveUsers(final Set<String> azureUserIds) {
        final List<User> localUsers = userRepository.findAll();
        for (final User localUser : localUsers) {
            if (!azureUserIds.contains(localUser.getAzureId())) {
                localUser.setActive(false);
                userRepository.save(localUser);
            }
        }
    }

    @Transactional
    void upsertUsersFromAzure(
            final List<com.microsoft.graph.models.User> azureUsers,
            final Set<Role> assignedRoles) {
        for (final com.microsoft.graph.models.User azureUser : azureUsers) {
            if (azureUser.mail == null) {
                continue;
            }

            final User existingUser = userRepository.findByAzureId(azureUser.id)
                    .orElseGet(() -> userRepository.findByEmail(azureUser.mail).orElse(null));

            if (existingUser == null) {
                final User newUser = new User();
                newUser.setAzureId(azureUser.id);
                newUser.setUsername(azureUser.userPrincipalName);
                newUser.setEmail(azureUser.mail);
                newUser.setFirstName(getFirstName(azureUser.displayName));
                newUser.setLastName(getLastName(azureUser.displayName));
                newUser.setPhoneNumber(azureUser.mobilePhone);
                newUser.setActive(true);
                final Set<UserRole> userRoles = assignedRoles.stream()
                        .map(role -> {
                            final UserRole ur = new UserRole();
                            ur.setUser(newUser);
                            ur.setRole(role);
                            return ur;
                        })
                        .collect(Collectors.toSet());
                newUser.setUserRoles(userRoles);
                userRepository.save(newUser);
            } else if (azureUserUpdater.updateUserFromAzure(existingUser, azureUser, assignedRoles)) {
                userRepository.save(existingUser);
            }
        }
    }

    void syncRolesFromAzure() {
        final List<Group> azureGroups = azureADService.getAllGroups();
        self.persistRolesFromAzure(azureGroups);
    }

    @Transactional
    void persistRolesFromAzure(final List<Group> azureGroups) {
        for (final Group group : azureGroups) {
            final Role role = roleRepository.findByName(group.displayName).orElse(null);
            if (role == null) {
                final Role newRole = new Role();
                newRole.setName(group.displayName);
                roleRepository.save(newRole);
            }
        }
    }

    private String getFirstName(final String displayName) {
        if (displayName == null) {
            return "";
        }
        return displayName.split(" ")[0];
    }

    private String getLastName(final String displayName) {
        if (displayName == null || !displayName.contains(" ")) {
            return "";
        }
        return displayName.substring(displayName.indexOf(" ") + 1);
    }
}
