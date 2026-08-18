package com.ndash.identity_framework.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SimpleUserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String companyName;
    private List<UserApplicationDto> applications;
    private boolean isActive;
    private Long managerId;
    private String managerName;

    public SimpleUserDto(Long id, String firstName, String lastName, String email, String companyName,
                         boolean active, Long managerId, String managerName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.companyName = companyName;
        this.isActive = active;
        this.managerId = managerId;
        this.managerName = managerName;
    }
}
