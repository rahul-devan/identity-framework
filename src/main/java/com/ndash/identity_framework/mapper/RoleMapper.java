package com.ndash.identity_framework.mapper;

import com.ndash.identity_framework.domain.Role;
import com.ndash.identity_framework.dto.RoleDto;

import java.util.stream.Collectors;

public class RoleMapper {

    public static RoleDto toDto(Role role) {
        if (role == null) return null;

        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());

        // Map users to UserDto
        dto.setUsers(
                role.getUsers().stream()
                        .map(UserMapper::toDto) // Assuming UserMapper exists
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
