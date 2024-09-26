package com.settlement.project.common.ads.entity;

import com.settlement.project.common.base.Timestamped;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Getter
@Entity
@NoArgsConstructor
@Table(name = "ads", indexes = {
        @Index(name = "idx_ads_is_used", columnList = "is_used")
})
public class Ad extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ad_id")
    private Long id;

    @Column(name = "ad_url")
    private String adUrl;

    @Column(name ="start_date")
    private LocalDate startDate;

    @Column(name ="end_date")
    private LocalDate endDate;

    @Column(name = "ad_playtime")
    private int adPlaytime;

    @Column(name = "is_used")
    private boolean isUsed = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AdStatusEnum status = AdStatusEnum.ACTIVE;

    @Version
    private Long version;  // 버전 필드 추가

    @Builder
    public Ad(String adUrl, LocalDate startDate, LocalDate endDate, boolean isUsed, int adPlaytime) {
        this.adUrl = adUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isUsed = isUsed;
        this.adPlaytime = adPlaytime;
        updateStatus(LocalDate.now());
    }

    public void updateStatus(LocalDate currentDate) {
        if (currentDate.isAfter(this.endDate)) {
            this.status = AdStatusEnum.EXPIRED;
        } else if (currentDate.isBefore(this.startDate)) {
            this.status = AdStatusEnum.SCHEDULED;
        } else {
            this.status = AdStatusEnum.ACTIVE;
        }
    }


    public void markAsUsed() {
        this.isUsed = true;
    }


    public void softDelete() {
        this.status = AdStatusEnum.EXPIRED;  // or a new status like DELETED
    }

    public void update(String adUrl, LocalDate startDate, LocalDate endDate, int adPlaytime) {
        this.adUrl = adUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.adPlaytime = adPlaytime;
    }
}
