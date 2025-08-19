package com.zactonics.apitemplates.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);  // Core threads
        executor.setMaxPoolSize(10);  // Max threads
        executor.setQueueCapacity(100);  // Queue size
        executor.setThreadNamePrefix("AsyncTask-");
        executor.initialize();
        return executor;
    }
}
