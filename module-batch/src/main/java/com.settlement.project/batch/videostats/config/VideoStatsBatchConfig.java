package com.settlement.project.batch.videostats.config;

import com.settlement.project.batch.videostats.service.VideoStatsBatchService;
import com.settlement.project.common.videostats.entity.VideoStats;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.batch.item.support.ListItemReader;
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
public class VideoStatsBatchConfig {
    private static final Logger log = LoggerFactory.getLogger(VideoStatsBatchConfig.class);

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final VideoStatsBatchService videoStatsBatchService;

    public VideoStatsBatchConfig(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 VideoStatsBatchService videoStatsBatchService) {
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
        this.videoStatsBatchService = videoStatsBatchService;
    }

    @Bean
    public Job updateVideoStatsJob() {
        return new JobBuilder("updateVideoStatsJob", jobRepository)
                .start(partitionedUpdateDailyStatsStep()) // 파티셔닝된 스텝 시작
                .build();
    }

    @Bean
    public Step partitionedUpdateDailyStatsStep() {
        return new StepBuilder("partitionedUpdateDailyStatsStep", jobRepository)
                .partitioner("updateDailyStatsStep", VideoStatsPartitioner())  // 파티셔닝 설정
                .step(updateDailyStatsStep())  // 각 파티션에서 실행될 Step
                .gridSize(5)
                .taskExecutor(videoStatsTaskExecutor())
                .build();
    }



    @Bean
    public Step updateDailyStatsStep() {
        return new StepBuilder("updateDailyStatsStep", jobRepository)
                .<Long, VideoStats>chunk(3000, transactionManager) // 청크 사이즈 3000
                .reader(videoIdReader())  // 각 파티션에 맞는 Reader 설정
                .processor(videoStatsProcessor())
                .writer(videoStatsWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Long> videoIdReader() {
        return new ItemReader<Long>() {
            private List<Long> videoIds;
            private int nextIndex;

            @PostConstruct
            public void init() {
                ExecutionContext executionContext =
                        StepSynchronizationManager.getContext().getStepExecution().getExecutionContext();
                int startIndex = executionContext.getInt("startIndex", 0);
                int endIndex = executionContext.getInt("endIndex", 0);

                List<Long> allVideoIds = videoStatsBatchService.getActiveVideoIds();
                this.videoIds = allVideoIds.subList(startIndex, endIndex);
                this.nextIndex = 0;
                log.info("Partition loaded {} video IDs for processing", this.videoIds.size());
            }

            @Override
            public Long read() {
                if (nextIndex < videoIds.size()) {
                    return videoIds.get(nextIndex++);
                }
                return null;
            }
        };
    }

    @Bean
    public ItemProcessor<Long, VideoStats> videoStatsProcessor() {
        return videoId -> {
             LocalDate date = LocalDate.now().minusDays(1);
//            // 임시 수정: 오늘의 데이터를 처리
//            LocalDate date = LocalDate.now();
            log.debug("Processing video ID: {} for date: {}", videoId, date);
            return videoStatsBatchService.processVideoStats(videoId, date);
        };
    }

    @Bean
    public ItemWriter<VideoStats> videoStatsWriter() {

        return chunk -> {
            log.info("Writing {} VideoStats", chunk.getItems().size());
            videoStatsBatchService.saveVideoStats(chunk.getItems());
        };
    }

    @Bean

    public TaskExecutor videoStatsTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(5);  // 기본 스레드 수
        taskExecutor.setMaxPoolSize(10);  // 최대 스레드 수
        taskExecutor.setQueueCapacity(25);  // 큐 크기
        taskExecutor.setThreadNamePrefix("VideoStats-");
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Bean
    public Partitioner VideoStatsPartitioner() {
        return gridSize -> {
            List<Long> allVideoIds = videoStatsBatchService.getActiveVideoIds();
            int totalSize = allVideoIds.size();
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

            log.info("Created {} partitions for {} video IDs", gridSize, totalSize);
            return partitions;
        };
    }
}