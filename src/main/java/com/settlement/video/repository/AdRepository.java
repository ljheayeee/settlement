package com.settlement.video.repository;

import com.settlement.video.entity.Ad;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.awt.print.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface AdRepository extends JpaRepository<Ad, Long> {

    @Query(value = "SELECT * FROM ad WHERE is_used = false ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Ad> findRandomUnusedAds(@Param("limit") int limit);

    @Modifying
    @Query(value = "UPDATE ad SET is_used = false WHERE is_used = true LIMIT :batchSize", nativeQuery = true)
    int resetUsedAdsInBatch(@Param("batchSize") int batchSize);
    // 이미 존재하는 메서드들
    List<Ad> findByIsUsedFalse();
    List<Ad> findAll();




}
