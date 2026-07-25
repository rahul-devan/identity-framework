package com.ndash.identity_framework.dto;

import lombok.Data;

import java.util.List;

@Data
public class SimpleUserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String companyName;
    private List<UserApplicationDto> applications;
}
