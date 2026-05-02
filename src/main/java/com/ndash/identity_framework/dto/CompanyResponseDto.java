package com.ndash.identity_framework.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompanyResponseDto {

    private Long id;
    private String name;
    private String location;
    private String phoneNumber;

    private String approverName;
    private Long approverId;

    private String contactName;
}
