package com.chatforyou.io.config;

import ch.qos.logback.core.util.StringUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;


/**
 * redis 를 master - slave 구조로 사용
 * master : 쓰기 작업 수행
 * slave : 읽기 작업 수행
 */
@Configuration
@EnableCaching
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.master.port}")
    private String masterPort;

    @Value("${spring.data.redis.slave.port}")
    private String slavePort;

    @Value("${spring.data.redis.password}")
    private String password;

    // redis 의 설정값을 세팅하기 위한 postConstruct
    // 환경변수로 url 과 port 가 들어오면 해당 값을 을 사용하고, 아니면 properties 에 정의 된 값을 사용
    @PostConstruct
    private void initRedis(){
        String envRedisUrl = System.getenv("REDIS_URL");
        if(!StringUtil.isNullOrEmpty(envRedisUrl)){
            host = envRedisUrl;
        }

        String envRedisMasterPort = System.getenv("REDIS_PORT");
        if(!StringUtil.isNullOrEmpty(envRedisMasterPort)){
            masterPort = envRedisMasterPort;
        }

        String envRedisSlavePort = System.getenv("REDIS_PORT");
        if(!StringUtil.isNullOrEmpty(envRedisSlavePort)){
            slavePort = envRedisSlavePort;
        }

        String envRedisPassword = System.getenv("REDIS_PASSWORD");
        if(!StringUtil.isNullOrEmpty(envRedisPassword)){
            password = envRedisPassword;
        }
    }

    @Primary
    @Bean("masterRedisConnectionFactory")
    public RedisConnectionFactory masterRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, Integer.parseInt(masterPort));
        return new LettuceConnectionFactory(config);
    }

    @Bean("slaveRedisConnectionFactory")
    public RedisConnectionFactory slaveRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, Integer.parseInt(slavePort));
        return new LettuceConnectionFactory(config);
    }

    @Primary
    @Bean(name = "masterRedisTemplate")
    public RedisTemplate<?, ?> masterRedisTemplate(@Qualifier("masterRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<byte[], byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        // 직렬화 및 역직렬화 설정
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer); // 이 부분이 데이터를 JSON 형식으로 직렬화
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean(name = "slaveRedisTemplate")
    public RedisTemplate<?, ?> slaveRedisTemplate(@Qualifier("slaveRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<byte[], byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        // 직렬화 및 역직렬화 설정
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer); // 이 부분이 데이터를 JSON 형식으로 직렬화
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

//    @Primary
//    @Bean(name = "masterCacheManager")
//    public CacheManager initMasterCacheManager(@Qualifier("masterRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory){
//        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
//                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())) // redis 에 저장될때 사용되는 시리얼라이즈 지정
//                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())); // redis 에 저장될때 사용되는 시리얼라이즈 지정
////                .entryTtl(Duration.ofMinutes(60L));
//
//        return RedisCacheManager
//                .RedisCacheManagerBuilder
//                .fromConnectionFactory(redisConnectionFactory)
//                .cacheDefaults(redisCacheConfiguration)
//                .build();
//    }
//
//    @Bean(name = "slaveCacheManager")
//    public CacheManager initSlaveCacheManager(@Qualifier("slaveRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory){
//        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
//                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
//                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
////                .entryTtl(Duration.ofMinutes(60L));
//
//        return RedisCacheManager
//                .RedisCacheManagerBuilder
//                .fromConnectionFactory(redisConnectionFactory)
//                .cacheDefaults(redisCacheConfiguration)
//                .build();
//    }

}
