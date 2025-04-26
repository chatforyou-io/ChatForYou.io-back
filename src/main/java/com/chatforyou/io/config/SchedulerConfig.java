package com.chatforyou.io.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@Slf4j
public class SchedulerConfig {

    @Value("${spring.thread.bound.multi}")
    private int ioBoundMultiplier;

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        log.info("############## ScheduledThreadPool Configuration :: Cores={}, PoolSize={}",
                corePoolSize, corePoolSize * ioBoundMultiplier);

        return Executors.newScheduledThreadPool(corePoolSize * ioBoundMultiplier);
    }
}
