package com.ndash.identity_framework.mapper;

import com.ndash.identity_framework.domain.*;
import com.ndash.identity_framework.dto.UserApplicationDto;
import com.ndash.identity_framework.dto.UserDto;
import com.ndash.identity_framework.util.CommonUtil;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {

    // Entity -> DTO (mask SSN)
    public static UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setEmail(user.getEmail());
        dto.setDob(user.getDob());
        dto.setMaskedSsn(CommonUtil.maskSSN(user.getSsn()));  // masked
        dto.setRoles(
                user.getUserRoles().stream()
                        .map(ur -> ur.getRole().getName())
                        .collect(Collectors.toSet())
        );
        dto.setNoIcon(CommonUtil.generateNoIcon(user.getFirstName(), user.getLastName()));
        if (user.getBlueprint() != null) {
            dto.setBlueprints(List.of(user.getBlueprint().getName()));
        }

        return dto;
    }

    // DTO -> Entity (raw SSN, password)
    public static User toEntity(UserDto dto, Set<Role> roles) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setEmail(dto.getEmail());
        user.setDob(dto.getDob());
        user.setSsn(dto.getSsn());
        Set<UserRole> userRoles = roles.stream().map(role -> {
            UserRole ur = new UserRole();
            ur.setUser(user);
            ur.setRole(role);
            ur.setId(new UserRoleId(user.getId(), role.getId()));
            return ur;
        }).collect(Collectors.toSet());
        user.setSource(dto.getSource());
        user.setUserRoles(userRoles);
        return user;
    }

    public static UserApplicationDto toUserApplicationDto(UserApplication ua) {

        UserApplicationDto dto = new UserApplicationDto();

        dto.setId(ua.getId());

        dto.setApplicationId(
                ua.getApplication().getId()
        );

        dto.setName(
                ua.getApplication().getName()
        );

        dto.setDescription(
                ua.getApplication().getDescription()
        );

        dto.setAccessLevel("Standard");

        dto.setGrantedDate(
                ua.getAssignedAt() != null
                        ? ua.getAssignedAt().toString()
                        : null
        );

        dto.setEssential(
                ua.getApplication().getName().equalsIgnoreCase("Slack")
                        || ua.getApplication().getName().equalsIgnoreCase("Jira")
        );

        dto.setActive(
                ua.isActive()
        );

        return dto;
    }
}
