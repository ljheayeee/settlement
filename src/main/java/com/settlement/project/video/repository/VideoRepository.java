package com.settlement.project.video.repository;

import com.settlement.project.video.entity.Video;
import com.settlement.project.video.entity.VideoStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RedisHash("Video")
public interface VideoRepository extends JpaRepository<Video, Long>, CrudRepository<Video, Long> {
    Optional<Video> findById(Long id);

    Page<Video> findByStatus(VideoStatusEnum status, Pageable pageable);
    Page<Video> findByStatusAndTitleContainingIgnoreCase(VideoStatusEnum status, String keyword, Pageable pageable);


    @Query(value = "SELECT id FROM videos", nativeQuery = true)
    List<Long> findAllVideoIds();

    @Query(value = "SELECT id FROM videos ORDER BY id OFFSET :offset LIMIT :limit", nativeQuery = true)
    List<Long> findVideoIdsPaginated(@Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "SELECT COUNT(*) FROM videos", nativeQuery = true)
    long countAllVideos();

    @Query("SELECT v.user.id FROM Video v WHERE v.id = :videoId")
    Optional<Long> findUserIdByVideoId(@Param("videoId") Long videoId);

    @Query("SELECT v.id FROM Video v WHERE v.status = 'ACTIVATE'")
    List<Long> findAllActiveVideoIds();


    @Query("SELECT v.id as id, v.title as title FROM Video v WHERE v.id IN :videoIds")
    List<VideoTitleProjection> findTitlesByVideoIds(@Param("videoIds") Set<Long> videoIds);

    interface VideoTitleProjection {
        Long getId();
        String getTitle();
    }
}
