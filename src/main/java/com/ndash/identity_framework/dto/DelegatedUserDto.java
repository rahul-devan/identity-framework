package com.ndash.identity_framework.dto;

public record DelegatedUserDto(
        Long id,
        String username,
        String email,
        String fullName,
        String jobTitle,
        String azureId,
        String departmentName)
{}
