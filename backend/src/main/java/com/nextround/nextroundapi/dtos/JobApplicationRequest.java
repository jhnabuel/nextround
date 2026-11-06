package com.nextround.nextroundapi.dtos;

import com.nextround.nextroundapi.enums.ApplicationStatus;
import com.nextround.nextroundapi.enums.WorkLocationType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record JobApplicationRequest(

        UUID companyId,

        @Size(max = 100, message = "Company name cannot exceed 100 characters")
        String companyName,

        @NotBlank(message = "Job title is required")
        @Size(max = 100, message = "Job title cannot exceed 100 characters")
        String jobTitle,

        @URL(message = "Job URL must be a valid URL")
        @Size(max = 500, message = "Job URL cannot exceed 500 characters")
        String jobUrl,

        ApplicationStatus applicationStatus,

        WorkLocationType workLocation,

        @PositiveOrZero(message = "Minimum salary cannot be negative")
        BigDecimal salaryMin,

        @PositiveOrZero(message = "Maximum salary cannot be negative")
        BigDecimal salaryMax,

        @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code (e.g., USD, PHP)")
        String currency,

        LocalDate appliedDate
) {
        @AssertTrue(message = "Either companyId or companyName must be provided")
        public boolean hasCompanyIdentifier() {
                return companyId != null || (companyName != null && !companyName.trim().isEmpty());
        }
}