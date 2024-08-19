package com.settlement.project.video.repository;

import com.settlement.project.video.entity.Video;
import com.settlement.project.video.entity.VideoStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<Video> findById(Long id);
    //test

    Optional<Video> findByUserId(Long UserId);  // 이 메서드 추가
    Page<Video> findByStatus(VideoStatusEnum status, Pageable pageable);
    Page<Video> findByStatusAndTitleContainingIgnoreCase(VideoStatusEnum status, String keyword, Pageable pageable);
    Optional<Video> findByTitle(String title);  // 이 메서드 추가
}
