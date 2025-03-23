package com.chatforyou.io.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@Slf4j
public class SchedulerConfig {
    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        log.info("############ Usable ScheduledThreadPool :: {} ", Runtime.getRuntime().availableProcessors());
        return Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }
}
