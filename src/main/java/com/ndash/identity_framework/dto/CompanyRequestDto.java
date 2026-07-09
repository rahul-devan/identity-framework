package com.ndash.identity_framework.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyRequestDto {

    @NotBlank
    private String name;

    private String location;
    private String phoneNumber;

    @NotNull
    private Long approverId;

    private Boolean isEnabled;

    @NotNull
    @Valid
    private ContactDto contact;
}
