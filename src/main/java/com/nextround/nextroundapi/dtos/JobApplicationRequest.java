package com.nextround.nextroundapi.dtos;

import com.nextround.nextroundapi.enums.ApplicationStatus;
import com.nextround.nextroundapi.enums.WorkLocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
public record JobApplicationRequest(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Company ID is required")
        UUID companyID,

        @NotBlank(message = "Job title is required")
        @Size(max = 100, message = "Job title cannot exceed 100 characters")
        String jobTitle,

        @URL(message = "Job URL must be a valid URL")
        @Size(max = 500, message = "Job URL cannot exceed 500 characters")
        String jobURL,

        ApplicationStatus applicationStatus,

        WorkLocationType workLocation,

        @PositiveOrZero(message = "Minimum salary cannot be negative")
        BigDecimal salaryMin,

        @PositiveOrZero(message = "Maximum salary cannot be negative")
        BigDecimal salaryMax,

        @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code (e.g., USD, PHP)")
        String currency,

        LocalDate appliedDate
) {}
