package com.nextround.nextroundapi.service;


import com.nextround.nextroundapi.dtos.DashboardStatsResponse;
import com.nextround.nextroundapi.enums.ApplicationStatus;
import com.nextround.nextroundapi.repository.JobApplicationRepository;
import com.nextround.nextroundapi.repository.JobApplicationStatusCount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DashboardService {
    private final JobApplicationRepository jobApplicationRepository;

    public DashboardService(JobApplicationRepository jobApplicationRepository){
        this.jobApplicationRepository = jobApplicationRepository;
    }

    public DashboardStatsResponse getStats(UUID userId){
        List<JobApplicationStatusCount> rows = jobApplicationRepository.countByApplicationStatus(userId, ApplicationStatus.WISHLIST);

        Map<ApplicationStatus, Long> counts = new EnumMap<>(ApplicationStatus.class);
        long totalApplications = 0L;

        for(JobApplicationStatusCount row : rows){
            counts.put(row.getStatus(), row.getCount());
            totalApplications += row.getCount();
        }

        return new DashboardStatsResponse(totalApplications,
                counts.getOrDefault(ApplicationStatus.APPLIED, 0L),
                counts.getOrDefault(ApplicationStatus.INTERVIEWING, 0L),
                counts.getOrDefault(ApplicationStatus.OFFER_RECEIVED, 0L),
                counts.getOrDefault(ApplicationStatus.REJECTED, 0L),
                counts.getOrDefault(ApplicationStatus.WITHDRAWN, 0L),
                counts.getOrDefault(ApplicationStatus.GHOSTED, 0L));
    }


}
