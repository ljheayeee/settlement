//package com.settlement.project.config;
//
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
//
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.repeat.RepeatStatus;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@EnableBatchProcessing
//public class TestBatchConfig {
//    @Autowired
//    public JobBuilder jobBuilderFactory;
//
//    @Autowired
//    public StepBuilder stepBuilderFactory;
//
//    @Bean
//    public Job testJob() {
//        return jobBuilderFactory.get("testJob")
//                .start(testStep())
//                .build();
//    }
//
//    @Bean
//    public Step testStep() {
//        return stepBuilderFactory.get("testStep")
//                .tasklet((contribution, chunkContext) -> {
//                    System.out.println("Test Step executed");
//                    return RepeatStatus.FINISHED;
//                })
//                .build();
//    }
//}
