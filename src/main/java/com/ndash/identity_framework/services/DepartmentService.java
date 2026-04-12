package com.ndash.identity_framework.services;

import com.ndash.identity_framework.dto.DepartmentResponseDTO;

import java.util.List;

public interface DepartmentService {

    List<DepartmentResponseDTO> getAllDepartments();
}