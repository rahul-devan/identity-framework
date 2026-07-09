package com.ndash.identity_framework.mapper;

import com.ndash.identity_framework.domain.Role;
import com.ndash.identity_framework.dto.RoleDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleStructMapper {

    RoleDto toSimpleDto(Role role);

    @Mapping(target = "userRoles", ignore = true)
    Role toEntity(RoleDto dto);
}
