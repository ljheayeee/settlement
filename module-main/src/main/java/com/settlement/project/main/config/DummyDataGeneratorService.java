package com.settlement.project.main.config;

import com.settlement.project.common.user.entity.User;
import com.settlement.project.common.user.entity.UserRoleEnum;
import com.settlement.project.common.user.repository.UserRepository;
import com.settlement.project.common.video.entity.Video;
import com.settlement.project.common.video.entity.VideoStatusEnum;
import com.settlement.project.common.video.repository.VideoRepository;
import com.settlement.project.main.ads.service.AdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
public class DummyDataGeneratorService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private AdService adService;

    private static final int SELLER_COUNT = 200;
    private static final int TOTAL_VIDEOS = 100000;
    private static final int BATCH_SIZE = 1000;

    public String generateDummyDataAndMeasurePerformance() {
        long start = System.currentTimeMillis();

        List<User> sellers = createSellers();
        createVideosWithViews(sellers);

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
        for (int i = 0; i < SELLER_COUNT; i++) {
            User seller = new User(
                    "seller" + i,
                    "password" + i,
                    "seller" + i + "@example.com",
                    UserRoleEnum.SELLER
            );
            sellers.add(seller);
        }
        return userRepository.saveAll(sellers);
    }

    private void createVideosWithViews(List<User> sellers) {
        Random random = new Random();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < TOTAL_VIDEOS; i += BATCH_SIZE) {
            int batchEnd = Math.min(i + BATCH_SIZE, TOTAL_VIDEOS);
            List<Video> videoBatch = new ArrayList<>();

            for (int j = i; j < batchEnd; j++) {
                User seller = sellers.get(random.nextInt(sellers.size()));
                int playTime = random.nextInt(3600) + 1;
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

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                saveVideosAndAssignAds(videoBatch);
            });
            futures.add(future);

            System.out.println("Created batch of videos: " + batchEnd);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    @Transactional
    public void saveVideosAndAssignAds(List<Video> videos) {
        try {
            List<Video> savedVideos = videoRepository.saveAll(videos);
            for (Video video : savedVideos) {
                adService.assignAdsToVideo(video.getId(), video.getPlayTimeInMinutes());
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