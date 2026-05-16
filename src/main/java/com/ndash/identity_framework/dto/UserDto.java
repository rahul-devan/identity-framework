package com.ndash.identity_framework.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ndash.identity_framework.domain.enums.UserSource;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private LocalDateTime dob;
    private String maskedSsn;        // masked version of SSN
    private Set<String> roles;       // role names instead of Role entity
    private String password;
    private String ssn;
    private String noIcon;
    private String departmentName; // For filtering by department
    private String departmentId;   // For filtering by department
    private String jobTitleName;
    private Long jobTitleId;
    private Set<SimpleUserDto> subordinates;
    private List<UserApplicationDto> applications;
    private UserSource source;
    private Long manager;

}
