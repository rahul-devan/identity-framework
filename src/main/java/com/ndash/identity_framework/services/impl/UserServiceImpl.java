package com.ndash.identity_framework.services.impl;

import com.ndash.identity_framework.domain.*;
import com.ndash.identity_framework.domain.enums.UserSource;
import com.ndash.identity_framework.dto.FetchTypeEnum;
import com.ndash.identity_framework.dto.AdminResetPasswordRequest;
import com.ndash.identity_framework.dto.ResetPasswordRequest;
import com.ndash.identity_framework.dto.SimpleUserDto;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.exception.ApiException;
import com.ndash.identity_framework.exception.BadRequestException;
import com.ndash.identity_framework.exception.ResourceNotFoundException;
import com.ndash.identity_framework.helper.AzureUserUpdater;
import com.ndash.identity_framework.mapper.UserMapper;
import com.ndash.identity_framework.repositories.*;
import com.ndash.identity_framework.services.AzureADService;
import com.ndash.identity_framework.services.AzureUserProvisioningService;
import com.ndash.identity_framework.services.UserCreationPersistence;
import com.ndash.identity_framework.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AzureADService azureADService;
    private final AzureUserUpdater azureUserUpdater;
    private final PasswordEncoder passwordEncoder;
    private final UserApplicationRepository userApplicationRepository;
    private final BlueprintRepository blueprintRepository;
    private final CompanyRepository companyRepository;
    private final UserCreationPersistence userCreationPersistence;
    private final AzureUserProvisioningService azureUserProvisioningService;

    @Value("${internal.default-company-name}")
    private String defaultCompanyName;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public UserDto createUser(UserDto userDto, Long loggedInUserId) {
        User savedUser = userCreationPersistence.createAndSave(userDto, loggedInUserId);
        azureUserProvisioningService.provisionUserAsync(
                savedUser.getId(),
                userDto.getFirstName(),
                userDto.getLastName(),
                userDto.getEmail()
        );
        log.info("User added to database with username: {}", savedUser.getUsername());
        return UserMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers(final FetchTypeEnum fetchTypeEnum) throws ApiException {
        try {
            List<User> users = findUsersByFetchType(fetchTypeEnum);
            Map<Long, Set<SimpleUserDto>> subordinatesByManager = loadActiveSubordinatesByManager();

            log.info("Fetched all users, total size: {}", users.size());

            return users.stream()
                    .map(user -> {
                        UserDto dto = UserMapper.toDto(user);
                        dto.setCompanyName(user.getCompany() != null ? user.getCompany().getName() : defaultCompanyName);
                        dto.setSubordinates(subordinatesByManager.getOrDefault(user.getId(), Collections.emptySet()));
                        return dto;
                    })
                    .collect(Collectors.toList());

        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Exception occurred while fetching users: {}", ex.getMessage());
            throw new ApiException(ex.getMessage() != null ? ex.getMessage() : "Failed to fetch users");
        }
    }

    private List<User> findUsersByFetchType(FetchTypeEnum fetchTypeEnum) {
        return switch (fetchTypeEnum) {
            case ALL -> userRepository.findAllWithDetails();
            case ACTIVE -> userRepository.findAllActiveWithDetails();
            case INACTIVE -> userRepository.findAllInactiveWithDetails();
        };
    }

    private Map<Long, Set<SimpleUserDto>> loadActiveSubordinatesByManager() {
        return userRepository.findAllActiveSubordinateRows(defaultCompanyName).stream()
                .collect(Collectors.groupingBy(
                        SubordinateProjection::getManagerId,
                        Collectors.mapping(this::toSimpleUserDto, Collectors.toSet())
                ));
    }

    private SimpleUserDto toSimpleUserDto(SubordinateProjection projection) {
        return new SimpleUserDto(
                projection.getId(),
                projection.getFirstName(),
                projection.getLastName(),
                projection.getEmail(),
                projection.getCompanyName(),
                projection.getActive(),
                projection.getManagerId(),
                projection.getManagerName()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id, final FetchTypeEnum fetchTypeEnum) {
        User user = userRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        UserDto dto = UserMapper.toDto(user);
        dto.setCompanyName(user.getCompany() != null ? user.getCompany().getName() : defaultCompanyName);
        dto.setSubordinates(getSubordinates(user.getId(), fetchTypeEnum));
        dto.setApplications(userApplicationRepository.findActiveApplicationDtosByUserId(id));
        return dto;
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // 1. Delete from Azure AD
        try {
            azureADService.deleteUser(user.getUsername());
        } catch (Exception ex) {
            throw new ApiException("Failed to delete user from Azure AD: " + ex.getMessage());
        }

        // 2. Delete from local DB
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> searchUsersByUsername(String username, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());

        Page<User> userPage =
                userRepository.findByUsernameContainingIgnoreCaseAndActiveTrue(username, pageable);

        Map<Long, Set<SimpleUserDto>> subordinatesByManager = loadActiveSubordinatesByManager();

        return userPage.map(user -> {
            UserDto dto = UserMapper.toDto(user);
            dto.setCompanyName(user.getCompany() != null ? user.getCompany().getName() : defaultCompanyName);
            dto.setSubordinates(subordinatesByManager.getOrDefault(user.getId(), Collections.emptySet()));
            return dto;
        });
    }


    @Override
    public UserDto updateUser(Long userId, UserDto userDto) throws ApiException {
        try {
            User existingUser = userRepository.findWithDetailsById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

            applyBasicFields(existingUser, userDto);
            applyRoles(existingUser, userDto);
            applyBlueprint(existingUser, userDto);
            applyCompanyAndManager(existingUser, userDto);

            if (Objects.isNull(existingUser.getSource())) {
                UserSource source = existingUser.getAzureId() != null ? UserSource.ENTRA : UserSource.APP;
                existingUser.setSource(source);
            }

            User updatedUser = userRepository.save(existingUser);
            log.info("User updated successfully: {}", updatedUser.getUsername());

            UserDto dto = UserMapper.toDto(updatedUser);
            dto.setCompanyName(updatedUser.getCompany() != null
                    ? updatedUser.getCompany().getName()
                    : defaultCompanyName);
            return dto;
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Exception occurred while updating user: {}", ex.getMessage(), ex);
            throw new ApiException(ex.getMessage() != null ? ex.getMessage() : "Failed to update user");
        }
    }

    @Override
    public UserDto updateUserActiveness(Long userId, UserDto userDto) throws ApiException {
        try {
            User existingUser = userRepository.findWithDetailsById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

            existingUser.setActive(userDto.isActive());
            User updatedUser = userRepository.save(existingUser);
            UserDto dto = UserMapper.toDto(updatedUser);
            dto.setCompanyName(updatedUser.getCompany() != null
                    ? updatedUser.getCompany().getName()
                    : defaultCompanyName);
            return dto;
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Exception occurred while updating user: {}", ex.getMessage(), ex);
            throw new ApiException(ex.getMessage() != null ? ex.getMessage() : "Failed to update user");
        }
    }

    private void applyBasicFields(User existingUser, UserDto userDto) {
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
        if (Objects.nonNull(userDto.getCountryCode())) {
            existingUser.setCountryCode(userDto.getCountryCode());
        }
    }

    private void applyRoles(User existingUser, UserDto userDto) {
        if (userDto.getRoles() == null || userDto.getRoles().isEmpty()) {
            return;
        }

        Set<String> requestedRoleNames = new HashSet<>(userDto.getRoles());
        Set<Role> requestedRoles = new HashSet<>(roleRepository.findByNameIn(requestedRoleNames));

        if (requestedRoles.size() != requestedRoleNames.size()) {
            throw new BadRequestException("One or more roles are invalid");
        }

        Set<UserRole> existingUserRoles = existingUser.getUserRoles();
        Set<Long> requestedRoleIds = requestedRoles.stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        existingUserRoles.removeIf(userRole -> !requestedRoleIds.contains(userRole.getRole().getId()));

        Set<Long> existingRoleIds = existingUserRoles.stream()
                .map(userRole -> userRole.getRole().getId())
                .collect(Collectors.toSet());

        for (Role role : requestedRoles) {
            if (!existingRoleIds.contains(role.getId())) {
                UserRole userRole = new UserRole();
                userRole.setUser(existingUser);
                userRole.setRole(role);
                existingUserRoles.add(userRole);
            }
        }
    }

    private void applyBlueprint(User existingUser, UserDto userDto) {
        if (userDto.getBlueprints() == null || userDto.getBlueprints().isEmpty()) {
            return;
        }

        String blueprintName = userDto.getBlueprints().get(0);
        Blueprint blueprint = blueprintRepository.findByNameIgnoreCase(blueprintName)
                .orElseThrow(() -> new BadRequestException("Invalid blueprint"));
        existingUser.setBlueprint(blueprint);
    }

    private void applyCompanyAndManager(User existingUser, UserDto userDto) {
        if (userDto.getCompanyId() != null) {
            Company company = companyRepository.findWithApproverById(userDto.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + userDto.getCompanyId()));
            existingUser.setCompany(company);
            if (company.getApprover() != null) {
                existingUser.setManager(company.getApprover());
            }
        }

        if (userDto.getManager() != null) {
            Long managerId = userDto.getManager();
            if (!userRepository.existsById(managerId)) {
                throw new BadRequestException("Invalid manager with id: " + managerId);
            }
            existingUser.setManager(userRepository.getReferenceById(managerId));
        }
    }

    @Override
    public void resetPassword(Long userId, ResetPasswordRequest request, Jwt jwt) throws ApiException {

        try {

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

            String loggedInEmail = jwt.getSubject();

            User loggedInUser = userRepository.findByEmail(loggedInEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Logged in user not found"));

            boolean isAdmin = loggedInUser.getUserRoles().stream()
                    .anyMatch(role -> role.getRole().getName().equalsIgnoreCase("administration") || role.getRole().getName().equalsIgnoreCase("super_admin"));

            // 🔹 ADMIN flow
            if (isAdmin) {

                if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
                    throw new BadRequestException("New password is required");
                }

                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                userRepository.save(user);

                log.info("Admin {} reset password for user {}", loggedInUser.getEmail(), user.getEmail());

                return;
            }

            // 🔹 NORMAL USER flow
            if (!loggedInUser.getId().equals(userId)) {
                throw new BadRequestException("You can only change your own password");
            }

            if (request.getOldPassword() == null || request.getNewPassword() == null) {
                throw new BadRequestException("Old password and new password are required");
            }

            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new BadRequestException("Old password is incorrect");
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            log.info("User {} changed their password", user.getEmail());

        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error resetting password", ex);
            throw new ApiException(ex.getMessage() != null ? ex.getMessage() : "Failed to reset password");
        }
    }

    @Override
    public void adminResetPassword(AdminResetPasswordRequest request, Jwt jwt) throws ApiException {
        try {
            if (request.getUserId() == null) {
                throw new BadRequestException("userId is required");
            }
            if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
                throw new BadRequestException("New password is required");
            }

            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles == null || roles.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions to reset password");
            }

            boolean isSuperAdmin = roles.stream()
                    .anyMatch(role -> "super_admin".equalsIgnoreCase(role));
            boolean isManager = roles.stream()
                    .anyMatch(role -> "manager".equalsIgnoreCase(role));

            if (!isSuperAdmin && !isManager) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Only super_admin or manager can reset another user's password"
                );
            }

            User targetUser = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + request.getUserId()));

            if (isManager && !isSuperAdmin) {
                Long actorUserId = extractUserIdFromJwt(jwt);
                if (targetUser.getManager() == null
                        || !targetUser.getManager().getId().equals(actorUserId)) {
                    throw new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "Managers can only reset passwords for their direct subordinates"
                    );
                }
            }

            targetUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(targetUser);

            log.info("User {} reset password for user id={}", extractUserIdFromJwt(jwt), request.getUserId());

        } catch (ApiException | ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error resetting password by admin/manager", ex);
            throw new ApiException(ex.getMessage() != null ? ex.getMessage() : "Failed to reset password");
        }
    }

    private Long extractUserIdFromJwt(Jwt jwt) {
        Object userIdClaim = jwt.getClaim("userId");
        if (userIdClaim instanceof Number number) {
            return number.longValue();
        }
        throw new BadRequestException("Invalid userId claim in token");
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByDepartment(Long departmentId) throws ApiException {
        try {
            List<User> users = userRepository.findByDepartmentIdAndActiveTrue(departmentId);
            log.info("Fetched users for department {}, total size: {}", departmentId, users.size());
            return users.stream()
                    .map(UserMapper::toDto)
                    .collect(Collectors.toList());
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Exception occurred while fetching users by department: {}", ex.getMessage());
            throw new ApiException(ex.getMessage() != null ? ex.getMessage() : "Failed to fetch users by department");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllManagers() throws ApiException {
        try {

            List<User> managers = userRepository.findAllManagers();

            return managers.stream()
                    .map(UserMapper::toDto)
                    .toList();

        } catch (Exception ex) {

            log.error("Exception occurred while fetching managers: {}", ex.getMessage(), ex);

            throw new ApiException("Failed to fetch manager users");
        }
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

    private Set<SimpleUserDto> getSubordinates(Long userId, final FetchTypeEnum fetchTypeEnum) {
        List<SimpleUserDto> subordinates = switch (fetchTypeEnum) {
            case ALL -> userRepository.findSubordinateProjectionsByManagerId(userId, defaultCompanyName);
            case ACTIVE -> userRepository.findActiveSubordinateProjectionsByManagerId(userId, defaultCompanyName);
            case INACTIVE -> userRepository.findInactiveSubordinateProjectionsByManagerId(userId, defaultCompanyName);
        };
        return new HashSet<>(subordinates);
    }

    private static String formatFullName(String firstName, String lastName) {
        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";
        return (first + " " + last).trim();
    }

}
