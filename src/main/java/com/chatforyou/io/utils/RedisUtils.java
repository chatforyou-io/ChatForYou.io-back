package com.chatforyou.io.utils;

import com.chatforyou.io.models.DataType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class RedisUtils {

    private final RedisTemplate<String, Object> masterTemplate;
    private final RedisTemplate<String, Object> slaveTemplate;
    private final ObjectMapper objectMapper;

    public RedisUtils(
            @Qualifier("masterRedisTemplate") RedisTemplate<String, Object> masterTemplate,
            @Qualifier("slaveRedisTemplate") RedisTemplate<String, Object> slaveTemplate,
            ObjectMapper objectMapper) {
        this.masterTemplate = masterTemplate;
        this.slaveTemplate = slaveTemplate;
        this.objectMapper = objectMapper;
    }


    /**
     * 객체를 JSON 으로 변환하여 Redis에 저장
     *
     * @param key    Redis 키
     * @param object 저장할 객체
     * @param <T>    객체 타입
     */
    public <T> void setObject(@NonNull String key, T object) {
        ValueOperations<String, Object> ops = masterTemplate.opsForValue();
        ops.set(key, object);  // RedisTemplate 이 자동으로 직렬화 처리
    }

    /**
     * 객체를 Redis에 저장하며, TTL(Expired Time) 설정
     *
     * @param key     Redis 키
     * @param object  저장할 객체
     * @param timeout 만료 시간 (TTL)
     * @param unit    시간 단위
     * @param <T>     객체 타입
     */
    public <T> void setObject(@NonNull String key, T object, long timeout, TimeUnit unit) {
        ValueOperations<String, Object> ops = masterTemplate.opsForValue();
        ops.set(key, object, timeout, unit);  // RedisTemplate 이 자동으로 직렬화 처리
    }

    /**
     * Redis에서 JSON 데이터를 가져와서 객체로 변환
     *
     * @param key   Redis 키
     * @param clazz 변환할 클래스 타입
     * @param <T>   객체 타입
     * @return 변환된 객체 (키가 존재하지 않으면 null)
     */
    public <T> T getObject(@NonNull String key, Class<T> clazz) {
        ValueOperations<String, Object> ops = slaveTemplate.opsForValue();
        Object value = ops.get(key);
        if (value == null) {
            return null;
        }
        return clazz.cast(value);  // 캐스트를 사용하여 값을 반환
    }

    public <T> T getObject(@NonNull String key, TypeReference<T> typeReference) {
        ValueOperations<String, Object> ops = slaveTemplate.opsForValue();
        String json = (String) ops.get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }


    /**
     * Redis에서 키를 삭제
     *
     * @param key Redis 키
     */
    public void delete(@NonNull String key) {
        masterTemplate.delete(key);
    }

    /**
     * Redis에 키가 존재하는지 확인
     *
     * @param key Redis 키
     * @return 키가 존재하면 true, 존재하지 않으면 false
     */
    public boolean hasKey(@NonNull String key) {
        return Boolean.TRUE.equals(slaveTemplate.hasKey(key));
    }

    /**
     * Redis expired 확인
     *
     * @param key Redis 키
     * @return 키가 존재하면 true, 존재하지 않으면 false
     */
    public long getExpired(@NonNull String key){
        return slaveTemplate.getExpire(key);
    }

    /**
     * Redis expired TimeUnit 으로 변환한 값
     *
     * @param key Redis 키
     * @param timeUnit timeUnit 으로 변환
     * @return 키가 존재하면 true, 존재하지 않으면 false
     */
    public long getExpiredByTimeUnit(@NonNull String key, TimeUnit timeUnit){
        return slaveTemplate.getExpire(key, timeUnit);
    }

    /**
     * redis 에 저장된 데이터 key 를 특정 pattern 에 맞춰 가져옴
     * @param pattern
     * @return pattern 에 맞는 key set
     */
    public Set<String> getKeysByPattern(String pattern) {
        Set<String> keys = new HashSet<>();

        // SCAN 옵션 설정 :: 와일드카드 검색할때는 뒤에 * 도 함께 붙여주자
        ScanOptions scanOptions = ScanOptions.scanOptions().match(pattern+"*").count(100).build();

        // Redis 커넥션에서 커서를 사용해 SCAN 명령 실행
        try (Cursor<byte[]> cursor = slaveTemplate.getConnectionFactory().getConnection().scan(scanOptions)) {
            while (cursor.hasNext()) {
                // 커서가 반환하는 키를 Set에 추가
                keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while scanning Redis keys", e);
        }

        return keys;
    }

    /**
     * redis 에 저장된 데이터를 가져와서 값을 count 만큼 증가시킨다
     * @param key
     * @return
     */
    public int incrementUserCount(String key, int count) {
        return masterTemplate.opsForValue().increment(key, count).intValue();
    }

    /**
     * user_count 값이 0인 키들을 검색한다
     * @return List<String> - 값이 0인 키들의 목록
     */
    public List<String> getSessionListForDelete() {
        String pattern = "*_"+ DataType.USER_COUNT+"*";
        List<String> sessionList = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();

        Cursor<byte[]> cursor = masterTemplate.getConnectionFactory().getConnection().scan(options);

        while (cursor.hasNext()) {
            String key = new String(cursor.next()).replace("\"", "");
            int userCount = (int)masterTemplate.opsForValue().get(key);
            if (0 == userCount) {
                sessionList.add(key.split("_")[0]);
            }
        }

        return sessionList;
    }

    public boolean deleteKeysBySessionId(String sessionId) {
        String pattern = "*" + sessionId + "*";
        try {
            // SCAN 명령어를 사용하여 키 검색 및 삭제
            ScanOptions scanOptions = ScanOptions.scanOptions().match(pattern).count(10).build();

            List<String> keysToDelete = new ArrayList<>();
            try (Cursor<byte[]> cursor = masterTemplate.execute(
                    (RedisCallback<Cursor<byte[]>>) connection -> connection.scan(scanOptions))) {

                while (cursor.hasNext()) {
                    keysToDelete.add(new String(cursor.next(), StandardCharsets.UTF_8).replace("\"", ""));
                }
            } catch (Exception e) {
                log.error("Error occurred while scanning and deleting keys ::: {}", e.getMessage());
                return false;
            }

            if (!keysToDelete.isEmpty()) {
                masterTemplate.delete(keysToDelete);
            }
            return true;
        }catch (RedisException e){
            log.error("UnExcepted Redis Exception ::: {}", Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

}
