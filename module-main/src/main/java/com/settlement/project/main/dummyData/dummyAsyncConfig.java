package com.settlement.project.main.dummyData;

import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component
public class dummyAsyncConfig {

    @Bean(name = "dummyTaskExecutor")
    public TaskExecutor dummytaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);  // 10개의 스레드 사용
        executor.setMaxPoolSize(10);   // 최대 20개의 스레드 사용 가능
        executor.setQueueCapacity(500);  // 대기열에 최대 500개의 작업
        executor.setThreadNamePrefix("VideoInsert-");
        executor.initialize();
        return executor;
    }
}