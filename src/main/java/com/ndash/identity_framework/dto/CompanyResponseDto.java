package com.ndash.identity_framework.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponseDto {

    private Long id;
    private String name;
    private String location;
    private String phoneNumber;

    private String approverName;
    private Long approverId;

    private Long primaryContactId;
    private String contactName;
}
