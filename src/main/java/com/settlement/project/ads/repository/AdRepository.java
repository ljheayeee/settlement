package com.settlement.project.ads.repository;

import com.settlement.project.ads.entity.Ad;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AdRepository extends JpaRepository<Ad, Long> {

    @Query(value = "SELECT * FROM ads WHERE is_used = false ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Ad> findRandomUnusedAds(@Param("limit") int limit);

    @Modifying
    @Query(value = "UPDATE ads SET is_used = false WHERE ad_id IN (SELECT ad_id FROM ads WHERE is_used = true ORDER BY ad_id LIMIT :batchSize)", nativeQuery = true)
    int resetUsedAdsInBatch(@Param("batchSize") int batchSize);

    List<Ad> findAllById(Iterable<Long> ids);  //
}
