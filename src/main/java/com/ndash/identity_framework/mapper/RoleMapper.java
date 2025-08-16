package com.ndash.identity_framework.mapper;

import com.ndash.identity_framework.domain.Role;
import com.ndash.identity_framework.domain.User;
import com.ndash.identity_framework.domain.UserRole;
import com.ndash.identity_framework.dto.RoleDto;

import java.util.Set;
import java.util.stream.Collectors;

public class RoleMapper {

    public static RoleDto toSimpleDto(Role role) {
        if (role == null) return null;
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        return dto;
    }

    public static RoleDto toDetailDto(Role role) {
        if (role == null) return null;
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setUsers(
                role.getUserRoles().stream()
                        .map(UserRole::getUser)
                        .filter(User::isActive)
                        .map(UserMapper::toDto)
                        .collect(Collectors.toSet())
        );
        return dto;
    }

    public static Role toEntity(RoleDto dto) {
        if (dto == null) return null;

        Role role = new Role();
        role.setId(dto.getId());
        role.setName(dto.getName());

        // Normally we don't map users here to avoid recursion
        return role;
    }
}
