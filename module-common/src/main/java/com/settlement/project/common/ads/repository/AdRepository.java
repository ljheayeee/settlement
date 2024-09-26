package com.settlement.project.common.ads.repository;

import com.settlement.project.common.ads.entity.Ad;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AdRepository extends JpaRepository<Ad, Long> {


    @Modifying
    @Query(value = "UPDATE ads SET is_used = false WHERE ad_id IN (SELECT ad_id FROM ads WHERE is_used = true ORDER BY ad_id LIMIT :batchSize)", nativeQuery = true)
    int resetUsedAdsInBatch(@Param("batchSize") int batchSize);

    List<Ad> findAllById(Iterable<Long> ids);  //


    @Query(value = "SELECT * FROM ads WHERE status = 'ACTIVE' AND is_used = false ORDER BY random() LIMIT :limit", nativeQuery = true)
    List<Ad> findRandomUnusedActiveAds(@Param("limit") int limit);

    Ad getAdById(Long adId);

    @Query("SELECT a FROM Ad a WHERE a.id BETWEEN :startRange AND :endRange")
    List<Ad> findAdsInRange(@Param("startRange") int startRange, @Param("endRange") int endRange);

}
