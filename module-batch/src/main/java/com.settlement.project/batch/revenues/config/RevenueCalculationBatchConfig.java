package com.settlement.project.batch.revenues.config;

import com.settlement.project.batch.revenues.service.RevenueBatchService;
import com.settlement.project.batch.videoadstats.VideoAdStatsBatchService;
import com.settlement.project.common.revenues.entity.Revenue;
import com.settlement.project.common.videostats.entity.VideoStats;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class RevenueCalculationBatchConfig {

    private static final Logger logger = LoggerFactory.getLogger(RevenueCalculationBatchConfig.class);
    private final RevenueBatchService revenueBatchService;
    private final VideoAdStatsBatchService videoAdStatsBatchService;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public RevenueCalculationBatchConfig(RevenueBatchService revenueBatchService, VideoAdStatsBatchService videoAdStatsBatchService, JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.revenueBatchService = revenueBatchService;
        this.videoAdStatsBatchService = videoAdStatsBatchService;
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job revenueCalculationJob() {
        return new JobBuilder("revenueCalculationJob", jobRepository)
                .start(partitionedRevenueCalculationStep())
                .next(updateVideoAdStatsStep())  // 새로 추가한 Step
                .build();
    }

    @Bean
    public Step partitionedRevenueCalculationStep() {
        return new StepBuilder("partitionedRevenueCalculationStep", jobRepository)
                .partitioner("revenueCalculationStep", RevenuePartitioner())
                .step(revenueCalculationStep())
                .gridSize(5)
                .taskExecutor(RevenueTaskExecutor())
                .build();
    }

    @Bean
    public Step revenueCalculationStep() {
        return new StepBuilder("revenueCalculationStep", jobRepository)
                .<VideoStats, Revenue>chunk(1000, transactionManager) // 청크 사이즈 1000으로 설정
                .reader(videoStatsReader(null, null))  // 파티션별로 videoStats를 읽어옴
                .processor(revenueProcessor())  // 수익 계산 처리
                .writer(revenueWriter())  // 청크 단위로 수익 데이터 저장
                .build();
    }

    @Bean
    public Step updateVideoAdStatsStep() {
        return new StepBuilder("updateVideoAdStatsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    videoAdStatsBatchService.updateTotalAndResetDailyViews();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<VideoStats> videoStatsReader(
            @Value("#{stepExecutionContext['startIndex']}") Integer startIndex,
            @Value("#{stepExecutionContext['endIndex']}") Integer endIndex) {

        return new ItemReader<VideoStats>() {
            private List<VideoStats> videoStats;
            private int nextIndex;

            @PostConstruct
            public void init() {
                LocalDate calculationDate = LocalDate.now(); // 수익 계산할 날짜 설정
                logger.info("Starting revenue calculation for date: {}", calculationDate);

                try {
                    // startIndex, endIndex에 따라 파티션 범위의 비디오 통계를 가져옴
                    this.videoStats = revenueBatchService.getVideoStatsForDate(calculationDate).subList(startIndex, endIndex);
                    this.nextIndex = 0;
                    logger.info("Partition loaded {} video stats for processing", this.videoStats.size());
                } catch (Exception e) {
                    logger.error("Error occurred during loading video stats for date: {}", calculationDate, e);
                    throw e;
                }
            }

            @Override
            public VideoStats read() {
                if (nextIndex < videoStats.size()) {
                    return videoStats.get(nextIndex++);
                }
                return null;
            }
        };
    }

    @Bean
    public ItemProcessor<VideoStats, Revenue> revenueProcessor() {
        return videoStats -> {
            try {
                // 수익 계산 처리 (비디오 통계를 기반으로 계산)
                return revenueBatchService.calculateRevenue(videoStats);
            } catch (Exception e) {
                logger.error("Error occurred during revenue calculation for video: {}", videoStats.getVideoId(), e);
                throw e;
            }
        };
    }

    @Bean
    public ItemWriter<Revenue> revenueWriter() {
        return chunk -> revenueBatchService.saveRevenues(chunk.getItems());
    }

    @Bean
    public Partitioner RevenuePartitioner() {
        return gridSize -> {
            // 전체 비디오 통계를 가져와 파티션으로 나눔
            List<VideoStats> allVideoStats = revenueBatchService.getVideoStatsForDate(LocalDate.now());
            int totalSize = allVideoStats.size();
            int partitionSize = (totalSize + gridSize - 1) / gridSize;

            Map<String, ExecutionContext> partitions = new HashMap<>(gridSize);
            for (int i = 0; i < gridSize; i++) {
                ExecutionContext context = new ExecutionContext();
                int startIndex = i * partitionSize;
                int endIndex = Math.min((i + 1) * partitionSize, totalSize);
                context.putInt("startIndex", startIndex);
                context.putInt("endIndex", endIndex);
                partitions.put("partition" + i, context);
            }

            logger.info("Created {} partitions for {} video stats", gridSize, totalSize);
            return partitions;
        };
    }

    @Bean
    public TaskExecutor RevenueTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(5);  // 기본 스레드 수
        taskExecutor.setMaxPoolSize(10);  // 최대 스레드 수
        taskExecutor.setQueueCapacity(25);  // 큐 크기
        taskExecutor.setThreadNamePrefix("Revenue-");
        taskExecutor.initialize();
        return taskExecutor;
    }
}
