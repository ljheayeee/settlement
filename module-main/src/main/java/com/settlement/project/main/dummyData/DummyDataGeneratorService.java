package com.settlement.project.main.dummyData;

import com.settlement.project.common.user.entity.User;
import com.settlement.project.common.user.entity.UserRoleEnum;
import com.settlement.project.common.user.repository.UserRepository;
import com.settlement.project.common.video.entity.Video;
import com.settlement.project.common.video.entity.VideoStatusEnum;
import com.settlement.project.common.video.repository.VideoRepository;
import com.settlement.project.main.ads.service.AdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class DummyDataGeneratorService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private AdService adService;

    private static final int TOTAL_VIDEOS = 10000;  // 총 1000개의 비디오 생성
    private static final int BATCH_SIZE = 100;  // 한 번에 100개씩 처리

    public String generateDummyDataAndMeasurePerformance() {
        long start = System.currentTimeMillis();

        List<User> sellers = createSellers();  // 셀러 생성
        createVideosWithViews(sellers);  // 비디오 생성 (동기 방식)

        long end = System.currentTimeMillis();
        long duration = end - start;

        int actualVideoCount = countVideos();
        int actualSellerCount = countSellers();

        return String.format("Dummy data generation completed. Time taken: %d ms. "
                        + "Sellers created: %d, Videos created: %d",
                duration, actualSellerCount, actualVideoCount);
    }

    @Transactional
    public List<User> createSellers() {
        List<User> sellers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {  // 총 20명의 셀러 생성
            User seller = new User(
                    "seller" + i,
                    "password" + i,
                    "seller" + i + "@example.com",
                    UserRoleEnum.SELLER
            );
            sellers.add(seller);
        }
        return userRepository.saveAll(sellers);  // 셀러 저장
    }

    private void createVideosWithViews(List<User> sellers) {
        Random random = new Random();

        for (int i = 0; i < TOTAL_VIDEOS; i += BATCH_SIZE) {
            int endRange = Math.min(i + BATCH_SIZE, TOTAL_VIDEOS);
            List<Video> videoBatch = new ArrayList<>();
            for (int j = i; j < endRange; j++) {
                User seller = sellers.get(random.nextInt(sellers.size()));
                int playTime = random.nextInt(1800 - 300 + 1) + 300;
                int views = random.nextInt(1500000) + 1;

                Video video = Video.builder()
                        .user(seller)
                        .title("Video " + j)
                        .description("Description for video " + j)
                        .videoUrl("https://example.com/video" + j)
                        .playTime(playTime)
                        .view(views)
                        .status(VideoStatusEnum.ACTIVATE)
                        .build();

                videoBatch.add(video);
            }

            saveVideosAndAssignAds(videoBatch);  // 동기적으로 비디오 저장 및 광고 할당
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveVideosAndAssignAds(List<Video> videos) {
        try {
            List<Video> savedVideos = videoRepository.saveAll(videos);  // 비디오 저장
            for (Video video : savedVideos) {
                adService.assignAdsToVideo(video.getId(), video.getPlayTimeInMinutes());  // 광고 할당
            }
        } catch (Exception e) {
            System.err.println("Error saving videos and assigning ads: " + e.getMessage());
        }
    }

    public int countVideos() {
        return (int) videoRepository.count();
    }

    public int countSellers() {
        return (int) userRepository.count();
    }
}
