package com.settlement.video.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Getter
@Entity
@NoArgsConstructor
@Table(name = "Ads")
public class Ad extends Timestamped{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ad_id")
    private Long id;

    @Column(name = "ad_url")
    private String adUrl;

    @Column(name ="end_date")
    private LocalDate endDate;

    @Column(name = "is_used")
    private boolean isUsed = false;

    @Builder
    public Ad(String adUrl, LocalDate endDate, boolean isUsed) {
        this.adUrl = adUrl;
        this.endDate = endDate;
        this.isUsed = isUsed;
    }
    public void markAsUsed() {
        this.isUsed = true;
    }

    public void resetUsage() {
        this.isUsed = false;
    }
}
