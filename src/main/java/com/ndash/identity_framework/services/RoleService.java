package com.ndash.identity_framework.services;

import com.ndash.identity_framework.dto.PaginatedResponse;
import com.ndash.identity_framework.dto.RoleDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RoleService {

    /**
     * Create a new role
     * @param roleDto DTO containing role details
     * @return Created role as DTO
     */
    RoleDto createRole(RoleDto roleDto);

    /**
     * Fetch all roles (non-paginated)
     * @return List of roles
     */
    List<RoleDto> getAllRoles();

    /**
     * Search roles by name with pagination
     * @param name  Role name or partial name to search
     * @param page  Page index (0-based)
     * @param size  Page size
     * @return PaginatedResponse of roles
     */
    Page<RoleDto> searchRolesByName(String name, int page, int size);

    /**
     * Get a specific role by ID
     * @param id Role ID
     * @return RoleDto if found
     */
    RoleDto getRoleById(Long id);

    /**
     * Delete a role by ID
     * @param id Role ID
     */
    void deleteRole(Long id);
}
