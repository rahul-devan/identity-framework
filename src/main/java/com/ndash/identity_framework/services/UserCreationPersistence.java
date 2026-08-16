package com.ndash.identity_framework.services;

import com.ndash.identity_framework.domain.*;
import com.ndash.identity_framework.domain.enums.UserSource;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.exception.ApiException;
import com.ndash.identity_framework.exception.BadRequestException;
import com.ndash.identity_framework.exception.DuplicateResourceException;
import com.ndash.identity_framework.exception.ResourceNotFoundException;
import com.ndash.identity_framework.mapper.UserMapper;
import com.ndash.identity_framework.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCreationPersistence {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BlueprintRepository blueprintRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    private volatile Role cachedDefaultRole;

    @Transactional
    public User createAndSave(UserDto userDto, Long loggedInUserId) throws ApiException {
        validateUniqueFields(userDto);

        Role defaultRole = getDefaultRole();
        Set<Role> assignedRoles = resolveRoles(userDto, defaultRole);

        User user = UserMapper.toEntity(userDto, assignedRoles);
        user.setUsername(userDto.getEmail());
        user.setSource(UserSource.APP);
        user.setActive(true);
        user.setCountryCode(userDto.getCountryCode());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setPassword(passwordEncoder.encode("Test@123"));

        applyBlueprint(user, userDto);
        applyCompanyAndManager(user, userDto, loggedInUserId);

        return userRepository.save(user);
    }

    private Role getDefaultRole() {
        Role role = cachedDefaultRole;
        if (role == null) {
            role = roleRepository.findByName("user")
                    .orElseThrow(() -> new ResourceNotFoundException("Default role 'user' not found in database"));
            cachedDefaultRole = role;
        }
        return role;
    }

    private Set<Role> resolveRoles(UserDto userDto, Role defaultRole) {
        if (userDto.getRoles() == null || userDto.getRoles().isEmpty()) {
            return Set.of(defaultRole);
        }

        Set<String> roleNames = new HashSet<>(userDto.getRoles());
        Map<String, Role> rolesByName = roleRepository.findByNameIn(roleNames).stream()
                .collect(Collectors.toMap(Role::getName, role -> role));

        return roleNames.stream()
                .map(name -> rolesByName.getOrDefault(name, defaultRole))
                .collect(Collectors.toSet());
    }

    private void applyBlueprint(User user, UserDto userDto) {
        if (userDto.getBlueprints() == null || userDto.getBlueprints().isEmpty()) {
            return;
        }

        String blueprintName = userDto.getBlueprints().get(0);
        Blueprint blueprint = blueprintRepository.findByNameIgnoreCase(blueprintName)
                .orElseThrow(() -> new BadRequestException("Invalid blueprint: " + blueprintName));
        user.setBlueprint(blueprint);
    }

    private void applyCompanyAndManager(User user, UserDto userDto, Long loggedInUserId) {
        if (userDto.getCompanyId() != null) {
            companyRepository.findById(userDto.getCompanyId()).ifPresent(company -> {
                user.setManager(company.getApprover());
                user.setCompany(company);
            });
            return;
        }

        userRepository.findById(loggedInUserId).ifPresent(loggedInUser -> {
            user.setManager(loggedInUser);
            user.setCompany(loggedInUser.getCompany());
        });
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
