package com.crs.iamservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Cấu hình Async để xử lý các tác vụ bất đồng bộ như gửi email
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);           // Số thread tối thiểu
        executor.setMaxPoolSize(5);            // Số thread tối đa
        executor.setQueueCapacity(100);        // Số task chờ trong queue
        executor.setThreadNamePrefix("Async-Email-");
        executor.initialize();
        return executor;
    }
}
