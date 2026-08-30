package com.nextround.nextroundapi.dtos;

import com.nextround.nextroundapi.entity.Company;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.enums.ApplicationStatus;
import com.nextround.nextroundapi.enums.WorkLocationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record JobApplicationResponse(UUID id, UserResponse user,
                                     CompanyResponse company,
                                     String jobTitle,
                                     String jobUrl,
                                     ApplicationStatus applicationStatus,
                                     WorkLocationType workLocation,
                                     BigDecimal salaryMin,
                                     BigDecimal salaryMax,
                                     String currency,
                                     LocalDate appliedDate,
                                     LocalDateTime createdAt,
                                     LocalDateTime updatedAt) {
    }

