package com.ndash.identity_framework.services.impl;

import com.ndash.identity_framework.domain.Role;
import com.ndash.identity_framework.dto.RoleDto;
import com.ndash.identity_framework.exception.ApiException;
import com.ndash.identity_framework.exception.ResourceNotFoundException;
import com.ndash.identity_framework.mapper.RoleMapper;
import com.ndash.identity_framework.repositories.RoleRepository;
import com.ndash.identity_framework.services.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public RoleDto createRole(RoleDto roleDto) {
        Role role = RoleMapper.toEntity(roleDto);
        return RoleMapper.toSimpleDto(roleRepository.save(role));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() throws ApiException {
        try {
            List<Role> roles = roleRepository.findAll();
            log.info("Fetched all roles, total size: {}", roles.size());
            return roles
                    .stream()
                    .map(RoleMapper::toSimpleDto)
                    .collect(Collectors.toList());
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Exception occurred while fetching roles: {}", ex.getMessage());
            throw new ApiException(ex.getMessage() != null ? ex.getMessage() : "Failed to fetch roles");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> searchRolesByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Role> rolePage = roleRepository.findByNameContainingIgnoreCase(name, pageable);
        return rolePage.getContent().stream()
                .map(RoleMapper::toSimpleDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDto getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return RoleMapper.toSimpleDto(role);
    }

    @Override
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }
}
