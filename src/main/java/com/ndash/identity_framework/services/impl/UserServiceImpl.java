package com.ndash.identity_framework.services.impl;

import com.ndash.identity_framework.domain.*;
import com.ndash.identity_framework.domain.enums.UserSource;
import com.ndash.identity_framework.dto.ResetPasswordRequest;
import com.ndash.identity_framework.dto.SimpleUserDto;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.exception.ApiException;
import com.ndash.identity_framework.exception.DuplicateResourceException;
import com.ndash.identity_framework.helper.AzureUserUpdater;
import com.ndash.identity_framework.mapper.UserMapper;
import com.ndash.identity_framework.repositories.*;
import com.ndash.identity_framework.services.AzureADService;
import com.ndash.identity_framework.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public UserDto createUser(UserDto userDto, Long loggedInUserId) throws ApiException {

        validateUniqueFields(userDto);
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
                user.setSource(UserSource.APP);

                // Assign default role
                UserRole userRole = new UserRole();
                userRole.setUser(user);
                userRole.setRole(defaultRole);
                user.setUserRoles(Set.of(userRole));
                Set<Role> assignedRoles;
                if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
                    assignedRoles = userDto.getRoles().stream()
                            .map(roleName -> roleRepository.findByName(roleName).orElse(defaultRole))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    Set<UserRole> userRoles = assignedRoles.stream().map(role -> {
                        UserRole ur = new UserRole();
                        ur.setUser(user);
                        ur.setRole(role);
                        ur.setId(new UserRoleId(user.getId(), role.getId()));
                        return ur;
                    }).collect(Collectors.toSet());
                    user.setUserRoles(userRoles);
                    log.info("Assigned roles: {}", assignedRoles);
                }

                if (userDto.getBlueprints() != null && !userDto.getBlueprints().isEmpty()) {

                    String blueprintName = userDto.getBlueprints().get(0);

                    Blueprint blueprint = blueprintRepository
                            .findByNameIgnoreCase(blueprintName)
                            .orElseThrow(() -> new RuntimeException("Invalid blueprint"));

                    user.setBlueprint(blueprint);
                }

                if(userDto.getCompanyId() != null){
                    Optional<Company> comppany = companyRepository.findById(userDto.getCompanyId());
                    if(comppany.isPresent()) {
                        log.info("Company found for user: {}, company name: {}", userDto.getEmail(), comppany.get().getName());
                        log.info("Manager is: {}", comppany.get().getApprover() != null ? comppany.get().getApprover().getEmail() : "No Manager");
                        user.setManager(comppany.get().getApprover());
                    }
                } else {
                    log.info("No companyId provided for user, setting manager as logged in user: {}", loggedInUserId);
                    user.setManager(userRepository.findById(loggedInUserId).orElse(null));
                }

                User savedUser = userRepository.save(user);
                if(savedUser.getAzureId() == null){
                    com.microsoft.graph.models.User azureUser =
                            azureADService.createUser(userDto.getFirstName(), userDto.getEmail());
                    if (azureUser == null || azureUser.id == null) {
                        log.error("ERROR Creating user in azure for user: {}", userDto.getEmail());
                    }
                }
                log.info("User already present in azure, synced to database");
                return UserMapper.toDto(savedUser);
            }

//             3. Create new user in Azure AD
            com.microsoft.graph.models.User azureUser =
                    azureADService.createUser(userDto.getFirstName(), userDto.getEmail());

            if (azureUser == null || azureUser.id == null) {
                log.error("ERROR Creating user in azure for user: {}", userDto.getEmail());
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
            user.setAzureId(azureUser != null ? azureUser.id : null);
            user.setUsername(userDto.getEmail());
            user.setSource(UserSource.APP);
            user.setActive(true);
            user.setCountryCode(userDto.getCountryCode());
            user.setPhoneNumber(userDto.getPhoneNumber());
            user.setPassword(passwordEncoder.encode("Test@123"));

            if (userDto.getBlueprints() != null && !userDto.getBlueprints().isEmpty()) {

                String blueprintName = userDto.getBlueprints().get(0);

                Blueprint blueprint = blueprintRepository
                        .findByNameIgnoreCase(blueprintName)
                        .orElseThrow(() -> new RuntimeException("Invalid blueprint"));

                user.setBlueprint(blueprint);
            }

            if(userDto.getCompanyId() != null){
                Optional<Company> comppany = companyRepository.findById(userDto.getCompanyId());
                if(comppany.isPresent()) {
                    log.info("Company found for user: {}, company name: {}", userDto.getEmail(), comppany.get().getName());
                    log.info("Manager is: {}", comppany.get().getApprover() != null ? comppany.get().getApprover().getEmail() : "No Manager");
                    user.setManager(comppany.get().getApprover());
                }
            } else {
                log.info("No companyId provided for user, setting manager as logged in user: {}", loggedInUserId);
                user.setManager(userRepository.findById(loggedInUserId).orElse(null));
            }

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
            List<User> users = userRepository.findAll();
            log.info("Fetched all users, total size: {}", users.size());

            return users.stream()
                    .map(user -> {
                        UserDto dto = UserMapper.toDto(user);
                        dto.setSubordinates(getSubordinates(user.getId())); // 👈 here
                        return dto;
                    })
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            log.error("Exception occurred while fetching users: {}", ex.getMessage());
            throw new ApiException(ex.getMessage());
        }
    }

    @Override
    public UserDto getUserById(Long id) {
        try {

            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<UserApplication> applications = userApplicationRepository.findByUserIdAndActiveTrue(id);
//            if(user.)

            UserDto dto = UserMapper.toDto(user);

            // 👇 Add subordinates here
            dto.setSubordinates(getSubordinates(user.getId()));

            // --------------------------------------
            // Main user applications
            // --------------------------------------
            dto.setApplications(
                    userApplicationRepository.findByUserIdAndActiveTrue(user.getId())
                            .stream()
                            .map(UserMapper::toUserApplicationDto)
                            .toList()
            );

            return dto;
        }catch (Exception ex) {
            log.error("Exception occurred while fetching user by id: {}", ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
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

        Page<User> userPage =
                userRepository.findByUsernameContainingIgnoreCaseAndActiveTrue(username, pageable);

        // 👇 Fetch all users once
        List<User> allUsers = userRepository.findAll();

        Map<Long, List<User>> subMap = allUsers.stream()
                .filter(u -> u.getManager() != null)
                .collect(Collectors.groupingBy(u -> u.getManager().getId()));

        return userPage.map(user -> {
            UserDto dto = UserMapper.toDto(user);

            List<User> subs = subMap.getOrDefault(user.getId(), Collections.emptyList());

            Set<SimpleUserDto> subDtos = subs.stream()
                    .map(u -> {
                        SimpleUserDto s = new SimpleUserDto();
                        s.setId(u.getId());
                        s.setFirstName(u.getFirstName());
                        s.setLastName(u.getLastName());
                        s.setEmail(u.getEmail());
                        return s;
                    })
                    .collect(Collectors.toSet());

            dto.setSubordinates(subDtos);
            return dto;
        });
    }


    @Override
    public UserDto updateUser(Long userId, UserDto userDto) throws ApiException {

        try {

            // 1. Get existing user
            User existingUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 2. Update Azure AD (optional)
            if (existingUser.getAzureId() != null) {

//                azureADService.updateUser(
//                        existingUser.getAzureId(),
//                        userDto.getFirstName(),
//                        userDto.getLastName(),
//                        userDto.getPhoneNumber()
//                );

                log.info("Azure AD user updated");
            }

            // 3. Update local DB fields
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

            // 4. Update roles
            if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
                // 1. Fetch all requested roles in one query
                Set<String> requestedRoleNames = new HashSet<>(userDto.getRoles());

                Set<Role> requestedRoles = new HashSet<>(
                        roleRepository.findByNameIn(requestedRoleNames)
                );

                if (requestedRoles.size() != requestedRoleNames.size()) {
                    throw new RuntimeException("One or more roles are invalid");
                }

                // 2. Existing mappings
                Set<UserRole> existingUserRoles = existingUser.getUserRoles();

                // 3. Requested role IDs
                Set<Long> requestedRoleIds = requestedRoles.stream()
                        .map(Role::getId)
                        .collect(Collectors.toSet());

                // 4. Remove roles no longer selected
                existingUserRoles.removeIf(
                        userRole -> !requestedRoleIds.contains(userRole.getRole().getId())
                );

                // 5. Existing role IDs after cleanup
                Set<Long> existingRoleIds = existingUserRoles.stream()
                        .map(userRole -> userRole.getRole().getId())
                        .collect(Collectors.toSet());

                // 6. Add only missing roles
                for (Role role : requestedRoles) {
                    if (!existingRoleIds.contains(role.getId())) {
                        UserRole userRole = new UserRole();
                        userRole.setUser(existingUser);
                        userRole.setRole(role);
                        existingUserRoles.add(userRole);
                    }
                }
            }
            if(Objects.nonNull(userDto.getCountryCode())){
                existingUser.setCountryCode(userDto.getCountryCode());
            }

            if (Objects.isNull(existingUser.getSource())) {
                UserSource source = existingUser.getAzureId() != null ? UserSource.ENTRA : UserSource.APP;
                existingUser.setSource(source);
            }

            if (userDto.getBlueprints() != null && !userDto.getBlueprints().isEmpty()) {

                String blueprintName = userDto.getBlueprints().get(0);

                Blueprint blueprint = blueprintRepository
                        .findByNameIgnoreCase(blueprintName)
                        .orElseThrow(() -> new RuntimeException("Invalid blueprint"));

                existingUser.setBlueprint(blueprint);
            }
            if (userDto.getCompanyId() != null) {
                Company company = companyRepository.findById(userDto.getCompanyId())
                        .orElseThrow(() -> new RuntimeException("Company not found"));
                existingUser.setCompany(company);
                if (company.getApprover() != null) {
                    existingUser.setManager(company.getApprover());
                }
            }

            // 5. Save
            User updatedUser = userRepository.save(existingUser);

            log.info("User updated successfully: {}", updatedUser.getUsername());

            return UserMapper.toDto(updatedUser);

        } catch (Exception ex) {
            log.error("Exception occurred while updating user: {}", ex.getMessage(), ex);
            throw new ApiException(ex.getMessage());
        }
    }

    @Override
    public void resetPassword(Long userId, ResetPasswordRequest request, Jwt jwt) throws ApiException {

        try {

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String loggedInEmail = jwt.getSubject();

            User loggedInUser = userRepository.findByEmail(loggedInEmail)
                    .orElseThrow(() -> new RuntimeException("Logged in user not found"));

            boolean isAdmin = loggedInUser.getUserRoles().stream()
                    .anyMatch(role -> role.getRole().getName().equalsIgnoreCase("administration") || role.getRole().getName().equalsIgnoreCase("super_admin"));

            // 🔹 ADMIN flow
            if (isAdmin) {

                if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
                    throw new RuntimeException("New password is required");
                }

                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                userRepository.save(user);

                log.info("Admin {} reset password for user {}", loggedInUser.getEmail(), user.getEmail());

                return;
            }

            // 🔹 NORMAL USER flow
            if (!loggedInUser.getId().equals(userId)) {
                throw new RuntimeException("You can only change your own password");
            }

            if (request.getOldPassword() == null || request.getNewPassword() == null) {
                throw new RuntimeException("Old password and new password are required");
            }

            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new RuntimeException("Old password is incorrect");
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            log.info("User {} changed their password", user.getEmail());

        } catch (Exception ex) {
            log.error("Error resetting password", ex);
            throw new ApiException(ex.getMessage());
        }
    }

    @Override
    public List<UserDto> getUsersByDepartment(Long departmentId) throws ApiException {
        try {
            List<User> users = userRepository.findByDepartmentIdAndActiveTrue(departmentId);
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

    private Set<SimpleUserDto> getSubordinates(Long userId) {

        return userRepository.findByManagerId(userId).stream().filter(User::isActive)
                .map(u -> {
                    SimpleUserDto dto = new SimpleUserDto();
                    dto.setId(u.getId());
                    dto.setFirstName(u.getFirstName());
                    dto.setLastName(u.getLastName());
                    dto.setEmail(u.getEmail());
                    return dto;
                })
                .collect(Collectors.toSet());
    }

    private void validateUniqueFields(UserDto userDto) throws ApiException {

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new DuplicateResourceException("A user with email '" +
                    userDto.getEmail() + "' already exists.");
        }

        if (userDto.getPhoneNumber() != null &&
                userRepository.existsByPhoneNumber(userDto.getPhoneNumber())) {
            throw new DuplicateResourceException("A user with mobile number '" +
                    userDto.getPhoneNumber() + "' already exists.");
        }

        if (userDto.getSsn() != null &&
                userRepository.existsBySsn(userDto.getSsn())) {
            throw new DuplicateResourceException("A user with SSN '" +
                    userDto.getSsn() + "' already exists.");
        }
    }

}
