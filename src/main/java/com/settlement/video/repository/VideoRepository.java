package com.settlement.video.repository;

import com.settlement.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<Video> findById(Long id);
    //test


    Optional<Video> findByTitle(String title);  // 이 메서드 추가
}
