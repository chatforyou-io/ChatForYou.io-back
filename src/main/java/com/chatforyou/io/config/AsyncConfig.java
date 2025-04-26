package com.chatforyou.io.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    @Value("${spring.thread.bound.multi}")
    private int ioBoundMultiplier;

    @Override
    @Bean("asyncTaskExecutor")
    public AsyncTaskExecutor getAsyncExecutor() {
        int coreCount = Runtime.getRuntime().availableProcessors();
        int poolSize = coreCount * ioBoundMultiplier;

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize * 2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("chatforyou.io-async-exec-");
        executor.initialize();
        return executor;
    }
}
