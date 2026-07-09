package com.ndash.identity_framework.services.impl;

import com.ndash.identity_framework.domain.Role;
import com.ndash.identity_framework.dto.RoleDto;
import com.ndash.identity_framework.exception.ApiException;
import com.ndash.identity_framework.mapper.RoleMapper;
import com.ndash.identity_framework.mapper.RoleStructMapper;
import com.ndash.identity_framework.repositories.RoleRepository;
import com.ndash.identity_framework.services.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleStructMapper roleStructMapper;

    public RoleServiceImpl(RoleRepository roleRepository, RoleStructMapper roleStructMapper) {
        this.roleRepository = roleRepository;
        this.roleStructMapper = roleStructMapper;
    }

    @Override
    public RoleDto createRole(RoleDto roleDto) {
        Role role = roleStructMapper.toEntity(roleDto);
        return roleStructMapper.toSimpleDto(roleRepository.save(role));
    }

    @Override
    @Cacheable(cacheNames = "roles")
    public List<RoleDto> getAllRoles() throws ApiException {
        try {
            List<Role> roles = roleRepository.findAll();
            log.info("Fetched all roles, total size: {}", roles.size());
            return roles
                    .stream()
                    .map(RoleMapper::toSimpleDto)
                    .collect(Collectors.toList());
        }catch (Exception ex){
            log.error("Exception occurred while fetching roles: {}", ex.getMessage());
            throw new ApiException(ex.getMessage());
        }
    }

    @Override
    public List<RoleDto> searchRolesByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Role> roles = roleRepository.findByNameContainingIgnoreCase(name);
        return roles.stream()
                .map(RoleMapper::toSimpleDto)
                .collect(Collectors.toList());
    }

    @Override
    public RoleDto getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return RoleMapper.toSimpleDto(role);
    }

    @Override
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }
}
