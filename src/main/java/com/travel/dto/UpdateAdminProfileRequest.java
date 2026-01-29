package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAdminProfileRequest {
    @NotBlank
    private String name;

    private String country;

    private String cityState;

    private String postalCode;
}
