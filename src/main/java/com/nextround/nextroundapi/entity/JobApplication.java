package com.nextround.nextroundapi.entity;

import com.nextround.nextroundapi.enums.ApplicationStatus;
import com.nextround.nextroundapi.enums.WorkLocationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="job_applications")
@Getter
@Setter
@NoArgsConstructor
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length =  100)
    private String jobTitle;

    @Column(length = 500)
    private String jobUrl;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkLocationType workLocation = WorkLocationType.ON_SITE;

    @Column(precision = 12, scale = 2)
    private BigDecimal salaryMin;

    @Column(precision = 12, scale = 2)
    private BigDecimal salaryMax;

    @Column(length = 3)
    private String currency = "PHP";

    private LocalDate appliedDate;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public JobApplication(
            User user,
            Company company,
            String jobTitle,
                   String jobUrl,
                   ApplicationStatus status,
                   WorkLocationType workLocation,
                   BigDecimal salaryMin,
                   BigDecimal salaryMax, String currency, LocalDate appliedDate){
            this.user = user;
            this.company = company;
            this.jobTitle = jobTitle;
            this.jobUrl = jobUrl;
            this.status = status != null ? status: ApplicationStatus.APPLIED;
            this.workLocation = workLocation != null ? workLocation: WorkLocationType.ON_SITE;
            this.salaryMin = salaryMin;
            this.salaryMax = salaryMax;
            this.currency = currency != null ? currency: "PHP";
            this.appliedDate = appliedDate != null ? appliedDate: LocalDate.now();
    }
}
