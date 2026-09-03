package com.nextround.nextroundapi.mapper;

import com.nextround.nextroundapi.dtos.CompanyResponse;
import com.nextround.nextroundapi.entity.Company;

public class CompanyMapper {
    public static CompanyResponse toDto(Company company){
        return new CompanyResponse(
                company.getId(),
                company.getCompanyName(),
                company.getWebsiteUrl(),
                company.getIndustry(),
                company.getLocation(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}