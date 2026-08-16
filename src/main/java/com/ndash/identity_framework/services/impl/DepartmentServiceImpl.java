package com.ndash.identity_framework.services.impl;

import com.ndash.identity_framework.domain.Department;
import com.ndash.identity_framework.dto.DepartmentResponseDTO;
import com.ndash.identity_framework.repositories.DepartmentRepository;
import com.ndash.identity_framework.services.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponseDTO> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private DepartmentResponseDTO mapToDTO(Department department) {
        DepartmentResponseDTO dto = new DepartmentResponseDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setExternalId(department.getExternalId());
        dto.setExternalSource(department.getExternalSource());
        return dto;
    }
}