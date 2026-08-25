package com.wakiyak.jobapplicationtracker.entity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Setter
@Getter
@Entity
@Table(name = "companies")
 public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String companyName;

    private String websiteUrl;

    private String industry;

    private String location;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    protected Company(){}

    public Company(String companyName, String websiteUrl, String industry, String location){
        this.companyName = companyName;
        this.websiteUrl = websiteUrl;
        this.industry = industry;
        this.location = location;
    }

}
