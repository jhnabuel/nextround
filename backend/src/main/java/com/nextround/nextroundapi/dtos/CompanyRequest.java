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

        @Size(max = 100, message = "Industry cannot exceed 100 characters")
        String industry,

        @Size(max = 100, message = "Location cannot exceed 100 characters")
        String location
) { }
