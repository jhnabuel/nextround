package com.nextround.nextroundapi.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record CompanyRequest(
        @NotBlank(message = "Company name is required")
        @Size(max = 100)
        String companyName,

        @URL(message = "Invalid website URL format")
        String websiteUrl,

        @NotBlank(message = "Industry is required")
        String industry,

        @NotBlank(message = "Location is required")
        String location
) { }
