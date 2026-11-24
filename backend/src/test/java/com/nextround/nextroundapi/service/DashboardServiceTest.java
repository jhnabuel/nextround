package com.nextround.nextroundapi.service;

import com.nextround.nextroundapi.dtos.DashboardActivityResponse;
import com.nextround.nextroundapi.dtos.DashboardStatsResponse;
import com.nextround.nextroundapi.enums.ApplicationStatus;
import com.nextround.nextroundapi.repository.JobApplicationActivityCount;
import com.nextround.nextroundapi.repository.JobApplicationRepository;
import com.nextround.nextroundapi.repository.JobApplicationStatusCount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getStats_mapsStatusesZeroFillsMissingAndTotalsAllSubmitted() {
        UUID userId = UUID.randomUUID();
        when(jobApplicationRepository.countByApplicationStatus(userId, ApplicationStatus.WISHLIST))
                .thenReturn(List.of(
                        statusCount(ApplicationStatus.APPLIED, 5),
                        statusCount(ApplicationStatus.INTERVIEWING, 2),
                        statusCount(ApplicationStatus.OFFER_RECEIVED, 1),
                        statusCount(ApplicationStatus.GHOSTED, 3)
                ));

        DashboardStatsResponse stats = dashboardService.getStats(userId);

        assertThat(stats.totalApplications()).isEqualTo(11);
        assertThat(stats.applied()).isEqualTo(5);
        assertThat(stats.interviewing()).isEqualTo(2);
        assertThat(stats.offers()).isEqualTo(1);
        assertThat(stats.rejected()).isZero();
    }

    @Test
    void getStats_zeroStateUser_returnsAllZeros() {
        UUID userId = UUID.randomUUID();
        when(jobApplicationRepository.countByApplicationStatus(userId, ApplicationStatus.WISHLIST))
                .thenReturn(List.of());

        DashboardStatsResponse stats = dashboardService.getStats(userId);

        assertThat(stats).isEqualTo(new DashboardStatsResponse(0L, 0L, 0L, 0L, 0L, 0L, 0L));
    }

    @Test
    void getActivity_weekly_returns12ContinuousZeroFilledBuckets() {
        UUID userId = UUID.randomUUID();
        LocalDate currentWeek = LocalDate.now(ZoneOffset.UTC)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate twoWeeksAgo = currentWeek.minusWeeks(2);

        when(jobApplicationRepository.countActivityByPeriod(eq(userId), eq("week"), any(LocalDateTime.class)))
                .thenReturn(List.of(
                        activityCount(twoWeeksAgo, 3),
                        activityCount(currentWeek, 4)
                ));

        DashboardActivityResponse response = dashboardService.getActivity(userId, "week");

        assertThat(response.granularity()).isEqualTo("WEEK");
        assertThat(response.data()).hasSize(12);
        assertThat(response.data().get(9).period()).isEqualTo(twoWeeksAgo.toString());
        assertThat(response.data().get(9).count()).isEqualTo(3);
        assertThat(response.data().get(10).count()).isZero();
        assertThat(response.data().get(11).period()).isEqualTo(currentWeek.toString());
        assertThat(response.data().get(11).count()).isEqualTo(4);
    }

    @Test
    void getActivity_monthly_returns6ContinuousFormattedBuckets() {
        UUID userId = UUID.randomUUID();
        LocalDate currentMonth = LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1);

        when(jobApplicationRepository.countActivityByPeriod(eq(userId), eq("month"), any(LocalDateTime.class)))
                .thenReturn(List.of(activityCount(currentMonth, 6)));

        DashboardActivityResponse response = dashboardService.getActivity(userId, "MONTH");

        assertThat(response.granularity()).isEqualTo("MONTH");
        assertThat(response.data()).hasSize(6);
        assertThat(response.data().get(5).count()).isEqualTo(6);
        assertThat(response.data().get(0).count()).isZero();
    }

    @Test
    void getActivity_invalidGranularity_throwsBadRequest() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> dashboardService.getActivity(userId, "DAILY"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private JobApplicationStatusCount statusCount(ApplicationStatus status, long count) {
        return new JobApplicationStatusCount() {
            @Override public ApplicationStatus getStatus() { return status; }
            @Override public long getCount() { return count; }
        };
    }

    private JobApplicationActivityCount activityCount(LocalDate period, long count) {
        return new JobApplicationActivityCount() {
            @Override public LocalDate getPeriod() { return period; }
            @Override public long getCount() { return count; }
        };
    }
}