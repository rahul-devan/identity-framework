package com.ndash.identity_framework.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ContactDto {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dob;
    private String ssn;
}
