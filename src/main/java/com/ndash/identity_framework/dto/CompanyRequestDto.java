package com.ndash.identity_framework.dto;

import lombok.Data;

@Data
public class CompanyRequestDto {

    private String name;
    private String location;
    private String phoneNumber;
    private Long approverId;
    private boolean isEnabled;

    private ContactDto contact;
}
