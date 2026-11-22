package com.nextround.nextroundapi.dtos;

public record DashboardStatsResponse(
        long totalApplications,
        long applied,
        long interviewing,
        long offers,
        long rejected,
        long withdrawn,
        long ghosted
) {
}
