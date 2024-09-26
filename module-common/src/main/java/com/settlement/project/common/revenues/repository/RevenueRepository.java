package com.settlement.project.common.revenues.repository;


import com.settlement.project.common.revenues.entity.Revenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RevenueRepository extends JpaRepository<Revenue, Long> {

    List<Revenue> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime);


    List<Revenue> findByCreatedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
}