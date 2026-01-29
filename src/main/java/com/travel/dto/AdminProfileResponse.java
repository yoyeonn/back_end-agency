package com.travel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String role;

    private String country;
    private String cityState;
    private String postalCode;

    private String imageUrl;
}
