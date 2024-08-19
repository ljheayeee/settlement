package com.settlement.project.watchhisotry.repository;

import com.settlement.project.user.entity.User;
import com.settlement.project.video.entity.Video;
import com.settlement.project.watchhisotry.entity.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {
    Optional<WatchHistory> findByUserIdAndVideoId(Long userId, Long videoId);

    Optional<WatchHistory> findByUserAndVideo(User user, Video video);
}
