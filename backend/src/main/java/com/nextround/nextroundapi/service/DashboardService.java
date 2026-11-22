package com.nextround.nextroundapi.service;


import com.nextround.nextroundapi.dtos.ActivityPoint;
import com.nextround.nextroundapi.dtos.DashboardActivityResponse;
import com.nextround.nextroundapi.dtos.DashboardStatsResponse;
import com.nextround.nextroundapi.enums.ApplicationStatus;
import com.nextround.nextroundapi.repository.JobApplicationActivityCount;
import com.nextround.nextroundapi.repository.JobApplicationRepository;
import com.nextround.nextroundapi.repository.JobApplicationStatusCount;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class DashboardService {
    private final JobApplicationRepository jobApplicationRepository;
    private static final int WEEKLY_LOOKBACK = 12;
    private static final int MONTHLY_LOOKBACK = 6;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

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


    public DashboardActivityResponse getActivity(UUID userId, String granularity){
        String normalizedGranularity = (granularity == null || granularity.isBlank()) ? "WEEK" : granularity.trim().toUpperCase(Locale.ROOT);

        boolean isWeekly;
        if("WEEK".equals(normalizedGranularity)){
            isWeekly=true;
        } else if ("MONTH".equals(normalizedGranularity)) {
            isWeekly=false;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid granularity. Must be WEEK or MONTH");
        }


        LocalDate today = LocalDate.now();
        LocalDate endBucket = isWeekly
                ? today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                : today.withDayOfMonth(1);
        LocalDate startBucket = isWeekly
                ? endBucket.minusWeeks(WEEKLY_LOOKBACK - 1L)
                : endBucket.minusMonths(MONTHLY_LOOKBACK - 1L);

        List<JobApplicationActivityCount> rows = jobApplicationRepository.countActivityByPeriod(
                userId,
                normalizedGranularity.toLowerCase(Locale.ROOT),
                startBucket.atStartOfDay()
        );

        Map<LocalDate, Long> countsByPeriod = new HashMap<>();
        for (JobApplicationActivityCount row : rows) {
            countsByPeriod.put(row.getPeriod(), row.getCount());
        }

        List<ActivityPoint> points = new ArrayList<>(isWeekly ? WEEKLY_LOOKBACK : MONTHLY_LOOKBACK);
        LocalDate cursor = startBucket;

        while (!cursor.isAfter(endBucket)) {
            long count = countsByPeriod.getOrDefault(cursor, 0L);
            String label = isWeekly ? cursor.toString() : cursor.format(MONTH_FORMATTER);
            points.add(new ActivityPoint(label, count));
            cursor = isWeekly ? cursor.plusWeeks(1) : cursor.plusMonths(1);
        }

        return new DashboardActivityResponse(normalizedGranularity, points);

    }

}
