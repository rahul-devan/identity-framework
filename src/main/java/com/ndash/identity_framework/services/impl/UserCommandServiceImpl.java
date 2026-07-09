package com.ndash.identity_framework.services.impl;

import com.ndash.identity_framework.domain.Blueprint;
import com.ndash.identity_framework.domain.Company;
import com.ndash.identity_framework.domain.Role;
import com.ndash.identity_framework.domain.User;
import com.ndash.identity_framework.domain.UserRole;
import com.ndash.identity_framework.domain.UserRoleId;
import com.ndash.identity_framework.domain.enums.UserSource;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.exception.ApiException;
import com.ndash.identity_framework.exception.DuplicateResourceException;
import com.ndash.identity_framework.mapper.UserMapper;
import com.ndash.identity_framework.repositories.BlueprintRepository;
import com.ndash.identity_framework.repositories.CompanyRepository;
import com.ndash.identity_framework.repositories.RoleRepository;
import com.ndash.identity_framework.repositories.UserRepository;
import com.ndash.identity_framework.security.RoleConstants;
import com.ndash.identity_framework.services.AuditService;
import com.ndash.identity_framework.services.AzureADService;
import com.ndash.identity_framework.services.UserCommandService;
import com.ndash.identity_framework.services.access.EntitlementProvisioner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AzureADService azureADService;
    private final PasswordEncoder passwordEncoder;
    private final BlueprintRepository blueprintRepository;
    private final CompanyRepository companyRepository;
    private final Optional<EntitlementProvisioner> entitlementProvisioner;
    private final AuditService auditService;
    private final UserCommandServiceImpl self;

    public UserCommandServiceImpl(
            final UserRepository userRepository,
            final RoleRepository roleRepository,
            final AzureADService azureADService,
            final PasswordEncoder passwordEncoder,
            final BlueprintRepository blueprintRepository,
            final CompanyRepository companyRepository,
            final Optional<EntitlementProvisioner> entitlementProvisioner,
            final AuditService auditService,
            @Lazy final UserCommandServiceImpl self) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.azureADService = azureADService;
        this.passwordEncoder = passwordEncoder;
        this.blueprintRepository = blueprintRepository;
        this.companyRepository = companyRepository;
        this.entitlementProvisioner = entitlementProvisioner;
        this.auditService = auditService;
        this.self = self;
    }

    @Override
    public UserDto createUser(final UserDto userDto, final Long loggedInUserId) throws ApiException {
        validateUniqueFields(userDto);
        final Role defaultRole = resolveDefaultRole();

        try {
            if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
                log.warn("User already exists in local database");
                throw new RuntimeException("User already exists in local database");
            }

            final com.microsoft.graph.models.User existingAzureUser =
                    azureADService.getUserByEmail(userDto.getEmail());

            if (existingAzureUser != null) {
                final User savedUser = self.persistExistingAzureUser(
                        userDto, loggedInUserId, defaultRole, existingAzureUser);

                if (savedUser.getAzureId() == null) {
                    final com.microsoft.graph.models.User azureUser =
                            azureADService.createUser(userDto.getFirstName(), userDto.getEmail());
                    if (azureUser == null || azureUser.id == null) {
                        log.error("ERROR Creating user in azure for user: {}", userDto.getEmail());
                    }
                }

                log.info("User already present in azure, synced to database");
                auditService.record("USER_SYNCED", "User", savedUser.getId(), loggedInUserId,
                        "email=" + savedUser.getEmail());
                return UserMapper.toDto(savedUser);
            }

            final com.microsoft.graph.models.User azureUser =
                    azureADService.createUser(userDto.getFirstName(), userDto.getEmail());

            if (azureUser == null || azureUser.id == null) {
                log.error("ERROR Creating user in azure for user: {}", userDto.getEmail());
            }
            log.info("User created in Azure AD");

            final User savedUser = self.persistNewUser(userDto, loggedInUserId, defaultRole, azureUser);
            auditService.record("USER_CREATED", "User", savedUser.getId(), loggedInUserId,
                    "email=" + savedUser.getEmail());
            return UserMapper.toDto(savedUser);

        } catch (Exception ex) {
            log.error("Exception occurred while creating user: {}", ex.getMessage(), ex);
            throw new ApiException(ex.getMessage());
        }
    }

    @Transactional
    User persistExistingAzureUser(
            final UserDto userDto,
            final Long loggedInUserId,
            final Role defaultRole,
            final com.microsoft.graph.models.User existingAzureUser) {
        final User user = new User();
        user.setAzureId(existingAzureUser.id);
        user.setUsername(existingAzureUser.userPrincipalName);
        user.setEmail(existingAzureUser.mail);
        user.setFirstName(getFirstName(existingAzureUser.displayName));
        user.setLastName(getLastName(existingAzureUser.displayName));
        user.setPhoneNumber(existingAzureUser.mobilePhone);
        user.setActive(true);
        user.setPassword("Test123");
        user.setSource(UserSource.APP);

        final UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(defaultRole);
        user.setUserRoles(Set.of(userRole));

        if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            final Set<Role> assignedRoles = userDto.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName).orElse(defaultRole))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            final Set<UserRole> userRoles = assignedRoles.stream().map(role -> {
                final UserRole ur = new UserRole();
                ur.setUser(user);
                ur.setRole(role);
                ur.setId(new UserRoleId(user.getId(), role.getId()));
                return ur;
            }).collect(Collectors.toSet());
            user.setUserRoles(userRoles);
            log.info("Assigned roles: {}", assignedRoles);
        }

        assignBlueprintAndManager(user, userDto, loggedInUserId);

        final User savedUser = userRepository.save(user);
        provisionEntitlements(savedUser);
        return savedUser;
    }

    @Transactional
    User persistNewUser(
            final UserDto userDto,
            final Long loggedInUserId,
            final Role defaultRole,
            final com.microsoft.graph.models.User azureUser) {
        final Set<Role> assignedRoles;
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

        final User user = UserMapper.toEntity(userDto, assignedRoles);
        user.setAzureId(azureUser != null ? azureUser.id : null);
        user.setUsername(userDto.getEmail());
        user.setSource(UserSource.APP);
        user.setActive(true);
        user.setCountryCode(userDto.getCountryCode());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setPassword(passwordEncoder.encode("Test@123"));

        assignBlueprintAndManager(user, userDto, loggedInUserId);

        final User savedUser = userRepository.save(user);
        log.info("User added to database with username: {}", savedUser.getUsername());
        provisionEntitlements(savedUser);
        return savedUser;
    }

    @Override
    @Transactional
    public UserDto updateUser(final Long userId, final UserDto userDto) throws ApiException {
        try {
            final User existingUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (existingUser.getAzureId() != null) {
                log.info("Azure AD user updated");
            }

            if (userDto.getFirstName() != null) {
                existingUser.setFirstName(userDto.getFirstName());
            }

            if (userDto.getLastName() != null) {
                existingUser.setLastName(userDto.getLastName());
            }

            if (userDto.getPhoneNumber() != null) {
                existingUser.setPhoneNumber(userDto.getPhoneNumber());
            }

            if (userDto.getEmail() != null) {
                existingUser.setEmail(userDto.getEmail());
            }

            if (userDto.getDob() != null) {
                existingUser.setDob(userDto.getDob());
            }

            if (userDto.getSsn() != null) {
                existingUser.setSsn(userDto.getSsn());
            }

            if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
                final Set<String> requestedRoleNames = new HashSet<>(userDto.getRoles());

                final Set<Role> requestedRoles = new HashSet<>(
                        roleRepository.findByNameIn(requestedRoleNames)
                );

                if (requestedRoles.size() != requestedRoleNames.size()) {
                    throw new RuntimeException("One or more roles are invalid");
                }

                final Set<UserRole> existingUserRoles = existingUser.getUserRoles();

                final Set<Long> requestedRoleIds = requestedRoles.stream()
                        .map(Role::getId)
                        .collect(Collectors.toSet());

                existingUserRoles.removeIf(
                        userRole -> !requestedRoleIds.contains(userRole.getRole().getId())
                );

                final Set<Long> existingRoleIds = existingUserRoles.stream()
                        .map(userRole -> userRole.getRole().getId())
                        .collect(Collectors.toSet());

                for (final Role role : requestedRoles) {
                    if (!existingRoleIds.contains(role.getId())) {
                        final UserRole userRole = new UserRole();
                        userRole.setUser(existingUser);
                        userRole.setRole(role);
                        existingUserRoles.add(userRole);
                    }
                }
            }

            if (Objects.nonNull(userDto.getCountryCode())) {
                existingUser.setCountryCode(userDto.getCountryCode());
            }

            if (Objects.isNull(existingUser.getSource())) {
                final UserSource source = existingUser.getAzureId() != null ? UserSource.ENTRA : UserSource.APP;
                existingUser.setSource(source);
            }

            if (userDto.getBlueprints() != null && !userDto.getBlueprints().isEmpty()) {
                final String blueprintName = userDto.getBlueprints().get(0);

                final Blueprint blueprint = blueprintRepository
                        .findByNameIgnoreCase(blueprintName)
                        .orElseThrow(() -> new RuntimeException("Invalid blueprint"));

                existingUser.setBlueprint(blueprint);
            }

            if (userDto.getCompanyId() != null) {
                final Company company = companyRepository.findById(userDto.getCompanyId())
                        .orElseThrow(() -> new RuntimeException("Company not found"));
                existingUser.setCompany(company);
                if (company.getApprover() != null) {
                    existingUser.setManager(company.getApprover());
                }
            }

            final User updatedUser = userRepository.save(existingUser);
            provisionEntitlements(updatedUser);
            auditService.record("USER_UPDATED", "User", updatedUser.getId(), null,
                    "username=" + updatedUser.getUsername());

            log.info("User updated successfully: {}", updatedUser.getUsername());

            return UserMapper.toDto(updatedUser);

        } catch (Exception ex) {
            log.error("Exception occurred while updating user: {}", ex.getMessage(), ex);
            throw new ApiException(ex.getMessage());
        }
    }

    @Override
    public void deleteUser(final Long id) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            azureADService.deleteUser(user.getUsername());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to delete user from Azure AD: " + ex.getMessage());
        }

        self.deleteUserFromDatabase(user);
        auditService.record("USER_DELETED", "User", user.getId(), null,
                "username=" + user.getUsername());
    }

    @Transactional
    void deleteUserFromDatabase(final User user) {
        userRepository.delete(user);
    }

    private Role resolveDefaultRole() {
        final Role defaultRole = roleRepository.findByName(RoleConstants.USER).orElse(null);
        if (defaultRole == null) {
            throw new RuntimeException("Default role '" + RoleConstants.USER + "' not found in DB");
        }
        return defaultRole;
    }

    private void assignBlueprintAndManager(
            final User user,
            final UserDto userDto,
            final Long loggedInUserId) {
        if (userDto.getBlueprints() != null && !userDto.getBlueprints().isEmpty()) {
            final String blueprintName = userDto.getBlueprints().get(0);

            final Blueprint blueprint = blueprintRepository
                    .findByNameIgnoreCase(blueprintName)
                    .orElseThrow(() -> new RuntimeException("Invalid blueprint"));

            user.setBlueprint(blueprint);
        }

        if (userDto.getCompanyId() != null) {
            final Optional<Company> company = companyRepository.findById(userDto.getCompanyId());
            if (company.isPresent()) {
                log.info("Company found for user: {}, company name: {}", userDto.getEmail(), company.get().getName());
                log.info("Manager is: {}",
                        company.get().getApprover() != null ? company.get().getApprover().getEmail() : "No Manager");
                user.setManager(company.get().getApprover());
            }
        } else {
            log.info("No companyId provided for user, setting manager as logged in user: {}", loggedInUserId);
            user.setManager(userRepository.findById(loggedInUserId).orElse(null));
        }
    }

    private void provisionEntitlements(final User user) {
        if (user.getBlueprint() == null) {
            return;
        }
        entitlementProvisioner.ifPresent(provisioner ->
                provisioner.provisionForBlueprintAssignment(user.getId(), user.getBlueprint().getId()));
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

    private void validateUniqueFields(final UserDto userDto) throws ApiException {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new DuplicateResourceException("A user with email '" +
                    userDto.getEmail() + "' already exists.");
        }

        if (userDto.getPhoneNumber() != null
                && userRepository.existsByPhoneNumber(userDto.getPhoneNumber())) {
            throw new DuplicateResourceException("A user with mobile number '" +
                    userDto.getPhoneNumber() + "' already exists.");
        }

        if (userDto.getSsn() != null
                && userRepository.existsBySsn(userDto.getSsn())) {
            throw new DuplicateResourceException("A user with SSN '" +
                    userDto.getSsn() + "' already exists.");
        }
    }
}
