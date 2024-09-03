//package com.settlement.project.ads.service;
//
//import com.settlement.project.ads.dto.AdRequestDto;
//import com.settlement.project.ads.dto.AdResponseDto;
//import com.settlement.project.ads.entity.Ad;
//import com.settlement.project.ads.entity.AdStatusEnum;
//import com.settlement.project.ads.repository.AdRepository;
//import com.settlement.project.videoadstats.service.VideoAdStatsService;
//import jakarta.persistence.EntityNotFoundException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.time.LocalDate;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//class AdServiceTest {
//
//    @Mock
//    private AdRepository adRepository;
//
//    @Mock
//    private VideoAdStatsService videoAdStatsService;
//
//    @InjectMocks
//    private AdService adService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    /**
//     * 테스트 목적: 비디오에 광고를 할당하는 기능을 검증한다.
//     * 검증 내용:
//     * 1. 적절한 수의 광고가 선택되는지 확인
//     * 2. StatsService가 호출되어 광고 통계가 생성되는지 확인
//     * 3. 선택된 광고들이 저장되는지 확인
//     */
//    @Test
//    @DisplayName("비디오에 광고 할당 테스트")
//    void testAssignAdsToVideo() {
//        Long videoId = 1L;
//        int videoLengthMinutes = 20;
//        List<Ad> mockAds = Arrays.asList(
//                new Ad("url1", LocalDate.now(), LocalDate.now().plusDays(30), false, 30),
//                new Ad("url2", LocalDate.now(), LocalDate.now().plusDays(30), false, 30),
//                new Ad("url3", LocalDate.now(), LocalDate.now().plusDays(30), false, 30)
//        );
//
////        when(adRepository.findRandomUnusedActiveAds(anyInt())
////                .thenReturn(mockAds);
//
//        adService.assignAdsToVideo(videoId, videoLengthMinutes);
//
//        verify(videoAdStatsService).createStatsForVideoAds(eq(videoId), anyList());
//        verify(adRepository, times(1)).saveAll(anyList());
//    }
//
//
//    /**
//     * 테스트 목적: 새로운 광고를 생성하는 기능을 검증한다.
//     * 검증 내용:
//     * 1. AdRequestDto로부터 Ad 엔티티가 올바르게 생성되는지 확인
//     * 2. 생성된 Ad가 저장소에 저장되는지 확인
//     * 3. 반환된 AdResponseDto가 입력된 데이터와 일치하는지 확인
//     */
//    @Test
//    @DisplayName("새로운 광고 생성 테스트")
//    void testCreateAd() {
//        AdRequestDto requestDto = AdRequestDto.builder()
//                .adUrl("https://example.com/ad")
//                .startDate(LocalDate.now())
//                .endDate(LocalDate.now().plusDays(30))
//                .adPlaytime(30)
//                .build();
//
//        Ad savedAd = new Ad(requestDto.getAdUrl(), requestDto.getStartDate(), requestDto.getEndDate(), false, requestDto.getAdPlaytime());
//        when(adRepository.save(any(Ad.class))).thenReturn(savedAd);
//
//        AdResponseDto responseDto = adService.createAd(requestDto);
//
//        assertNotNull(responseDto);
//        assertEquals(requestDto.getAdUrl(), responseDto.getAdUrl());
//        assertEquals(requestDto.getStartDate(), responseDto.getStartDate());
//        assertEquals(requestDto.getEndDate(), responseDto.getEndDate());
//        assertEquals(requestDto.getAdPlaytime(), responseDto.getAdPlaytime());
//        assertEquals(AdStatusEnum.ACTIVE, responseDto.getStatus());
//    }
//
//    /**
//     * 테스트 목적: 기존 광고를 업데이트하는 기능을 검증한다.
//     * 검증 내용:
//     * 1. 주어진 ID의 광고가 존재하는지 확인
//     * 2. AdRequestDto의 데이터로 광고가 올바르게 업데이트되는지 확인
//     * 3. 업데이트된 광고가 저장소에 저장되는지 확인
//     * 4. 반환된 AdResponseDto가 업데이트된 데이터와 일치하는지 확인
//     */
//    @Test
//    @DisplayName("기존 광고 업데이트 테스트")
//    void testUpdateAd() {
//        Long adId = 1L;
//        AdRequestDto requestDto = AdRequestDto.builder()
//                .adUrl("https://example.com/updated-ad")
//                .startDate(LocalDate.now())
//                .endDate(LocalDate.now().plusDays(60))
//                .adPlaytime(45)
//                .build();
//
//        Ad existingAd = new Ad("https://example.com/old-ad", LocalDate.now().minusDays(10), LocalDate.now().plusDays(20), false, 30);
//        when(adRepository.findById(adId)).thenReturn(Optional.of(existingAd));
//        when(adRepository.save(any(Ad.class))).thenReturn(existingAd);
//
//        AdResponseDto responseDto = adService.updateAd(adId, requestDto);
//
//        assertNotNull(responseDto);
//        assertEquals(requestDto.getAdUrl(), responseDto.getAdUrl());
//        assertEquals(requestDto.getStartDate(), responseDto.getStartDate());
//        assertEquals(requestDto.getEndDate(), responseDto.getEndDate());
//        assertEquals(requestDto.getAdPlaytime(), responseDto.getAdPlaytime());
//    }
//
//    /**
//     * 테스트 목적: 광고를 소프트 삭제하는 기능을 검증한다.
//     * 검증 내용:
//     * 1. 주어진 ID의 광고가 존재하는지 확인
//     * 2. 광고의 상태가 EXPIRED로 변경되는지 확인
//     * 3. 상태가 변경된 광고가 저장소에 저장되는지 확인
//     */
//    @Test
//    @DisplayName("광고 소프트 삭제 테스트")
//    void testDeleteAd() {
//        Long adId = 1L;
//        Ad existingAd = new Ad("https://example.com/ad", LocalDate.now(), LocalDate.now().plusDays(30), false, 30);
//        when(adRepository.findById(adId)).thenReturn(Optional.of(existingAd));
//
//        adService.softDeleteAd(adId);
//
//        verify(adRepository).save(existingAd);
//        assertEquals(AdStatusEnum.EXPIRED, existingAd.getStatus());
//    }
//
//    /**
//     * 테스트 목적: ID로 광고를 조회하는 기능을 검증한다.
//     * 검증 내용:
//     * 1. 주어진 ID의 광고가 존재하는지 확인
//     * 2. 반환된 광고의 데이터가 예상한 데이터와 일치하는지 확인
//     */
//    @Test
//    @DisplayName("ID로 광고 조회 테스트")
//
//    void testGetAdById() {
//        Long adId = 1L;
//        Ad ad = new Ad("https://example.com/ad", LocalDate.now(), LocalDate.now().plusDays(30), false, 30);
//        when(adRepository.findById(adId)).thenReturn(Optional.of(ad));
//
//        Ad result = adService.getAdById(adId);
//
//        assertNotNull(result);
//        assertEquals(ad.getAdUrl(), result.getAdUrl());
//    }
//
//
//    /**
//     * 테스트 목적: 존재하지 않는 ID로 광고를 조회할 때의 예외 처리를 검증한다.
//     * 검증 내용:
//     * 1. 존재하지 않는 ID로 조회 시 EntityNotFoundException이 발생하는지 확인
//     */
//    @Test
//    @DisplayName("존재하지 않는 광고 조회 시 예외 발생 테스트")
//    void testGetAdByIdNotFound() {
//        Long adId = 1L;
//        when(adRepository.findById(adId)).thenReturn(Optional.empty());
//
//        assertThrows(EntityNotFoundException.class, () -> adService.getAdById(adId));
//    }
//
//
//    /**
//     * 테스트 목적: 모든 광고의 상태를 업데이트하는 기능을 검증한다.
//     * 검증 내용:
//     * 1. 모든 광고가 조회되는지 확인
//     * 2. 각 광고의 상태가 현재 날짜를 기준으로 올바르게 업데이트되는지 확인
//     * 3. 업데이트된 광고들이 저장소에 저장되는지 확인
//     */
//    @Test
//    @DisplayName("모든 광고 상태 업데이트 테스트")
//    void testUpdateAdStatuses() {
//        List<Ad> ads = Arrays.asList(
//                new Ad("url1", LocalDate.now().minusDays(10), LocalDate.now().plusDays(10), false, 30),
//                new Ad("url2", LocalDate.now().plusDays(5), LocalDate.now().plusDays(15), false, 30),
//                new Ad("url3", LocalDate.now().minusDays(20), LocalDate.now().minusDays(10), false, 30)
//        );
//
//        when(adRepository.findAll()).thenReturn(ads);
//
//        adService.updateAdStatuses();
//
//        verify(adRepository).saveAll(ads);
//        assertEquals(AdStatusEnum.ACTIVE, ads.get(0).getStatus());
//        assertEquals(AdStatusEnum.SCHEDULED, ads.get(1).getStatus());
//        assertEquals(AdStatusEnum.EXPIRED, ads.get(2).getStatus());
//    }
//}