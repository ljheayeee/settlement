package com.settlement.video.config;

import com.settlement.video.entity.Ad;
import com.settlement.video.repository.AdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

@Configuration
public class AdBatchConfig {
    private static final Logger logger = LoggerFactory.getLogger(AdBatchConfig.class);

    private final JobRepository jobRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final AdRepository adRepository;
    private final PlatformTransactionManager transactionManager;

    public AdBatchConfig(JobRepository jobRepository,
                         EntityManagerFactory entityManagerFactory,
                         AdRepository adRepository,
                         PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.entityManagerFactory = entityManagerFactory;
        this.adRepository = adRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job removeExpiredAdsJob() {
        return new JobBuilder("removeExpiredAdsJob", jobRepository)
                .start(removeExpiredAdsStep())
                .build();
    }

    @Bean
    public Step removeExpiredAdsStep() {
        return new StepBuilder("removeExpiredAdsStep", jobRepository)
                .<Ad, Ad>chunk(100, transactionManager)
                .reader(expiredAdReader())
                .processor(adProcessor())
                .writer(adWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Ad> expiredAdReader() {
        return new JpaPagingItemReaderBuilder<Ad>()
                .name("expiredAdReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(100)
                .queryString("SELECT a FROM Ad a WHERE a.endDate < CURRENT_DATE")
                .build();
    }

    @Bean
    public ItemProcessor<Ad, Ad> adProcessor() {
        return ad -> {
            logger.info("Processing expired ad: {}", ad.getId());
            return ad;
        };
    }

    @Bean
    public ItemWriter<Ad> adWriter() {
        return chunk -> {
            for (Ad ad : chunk) {
                try {
                    adRepository.delete(ad);
                    logger.info("Deleted expired ad: {}", ad.getId());
                } catch (Exception e) {
                    logger.error("Error deleting ad {}: {}", ad.getId(), e.getMessage());
                }
            }
        };
    }
}