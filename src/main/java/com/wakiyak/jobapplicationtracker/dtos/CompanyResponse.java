package com.wakiyak.jobapplicationtracker.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CompanyResponse(UUID id,
                              String companyName,
                              String websiteUrl,
                              String location) {
}
