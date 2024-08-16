package com.settlement.video.service;

import com.settlement.video.dto.VideoRequestDto;
import com.settlement.video.entity.Ad;
import com.settlement.video.entity.User;
import com.settlement.video.entity.UserRoleEnum;
import com.settlement.video.entity.Video;
import com.settlement.video.repository.AdRepository;
import com.settlement.video.repository.StatsRepository;
import com.settlement.video.repository.UserRepository;
import com.settlement.video.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Transactional
public class VideoServiceCreateLongVideoIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(VideoServiceCreateLongVideoIntegrationTest.class);

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdRepository adRepository;

    @Autowired
    private StatsRepository statsRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성 및 저장
        testUser = new User("testUser", "password", "test@example.com", UserRoleEnum.USER);
        userRepository.save(testUser);

        // 테스트용 광고 10,000개 생성 및 저장
        List<Ad> ads = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            Ad ad = new Ad("http://example.com/ad" + i, LocalDate.now().plusDays(30), false);
            ads.add(ad);
        }
        adRepository.saveAll(ads);

        logger.info("테스트 데이터 준비 완료: 사용자 1명, 광고 10,000개 생성");
    }

    @Test
    void measureCreateLongVideoPerformanceTest() {
        logger.info("24시간 비디오 생성에 대한 assignAdsToVideo 성능 테스트를 시작합니다.");

        VideoRequestDto requestDto = VideoRequestDto.builder()
                .userId(testUser.getId())
                .title("24시간 테스트 비디오 (New)")
                .description("24시간 길이의 긴 테스트 비디오")
                .videoUrl("http://example.com/longvideo_new")
                .playTime(86400) // 24시간 비디오 (24 * 60 * 60 초)
                .build();

        logger.info("생성된 VideoRequestDto: {}", requestDto);

        long startTime = System.nanoTime();
        videoService.createVideo(requestDto);  // 이 메소드 내부에서 assignAdsToVideo가 호출됩니다.
        long endTime = System.nanoTime();
        long executionTime = endTime - startTime;

        logger.info("24시간 비디오에 대한 assignAdsToVideo 실행 시간: {} 나노초", executionTime);
        logger.info("24시간 비디오에 대한 assignAdsToVideo 실행 시간: {} 밀리초", executionTime / 1_000_000);

        Video savedVideo = videoRepository.findByTitle("24시간 테스트 비디오 (New)")
                .orElseThrow(() -> new AssertionError("저장된 비디오를 찾을 수 없습니다."));
        long assignedAdsCount = statsRepository.countByVideoId(savedVideo.getId());
        logger.info("assignAdsToVideo: 비디오에 할당된 광고 수: {}", assignedAdsCount);
    }
}