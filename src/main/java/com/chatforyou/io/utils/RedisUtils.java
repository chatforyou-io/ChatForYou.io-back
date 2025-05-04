package com.chatforyou.io.utils;

import ch.qos.logback.core.util.StringUtil;
import com.chatforyou.io.models.DataType;
import com.chatforyou.io.models.OpenViduDto;
import com.chatforyou.io.models.RedisIndex;
import com.chatforyou.io.models.in.ChatRoomInVo;
import com.chatforyou.io.models.out.ConnectionOutVo;
import com.chatforyou.io.models.out.UserOutVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dengliming.redismodule.redisearch.RediSearch;
import io.github.dengliming.redismodule.redisearch.client.RediSearchClient;
import io.github.dengliming.redismodule.redisearch.index.Document;
import io.github.dengliming.redismodule.redisearch.search.SearchOptions;
import io.github.dengliming.redismodule.redisearch.search.SortBy;
import io.lettuce.core.RedisException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.SortOrder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Component
@Slf4j
public class RedisUtils {

    private final RedisTemplate<String, Object> masterTemplate;
    private final RedisTemplate<String, Object> slaveTemplate;
    private final ObjectMapper objectMapper;
    private final RediSearchClient rediSearchClient;
    private final long REDIS_TIMEOUT = 1L;

    public RedisUtils(
            @Qualifier("masterRedisTemplate") RedisTemplate<String, Object> masterTemplate,
            @Qualifier("slaveRedisTemplate") RedisTemplate<String, Object> slaveTemplate,
            ObjectMapper objectMapper, RediSearchClient rediSearchClient) {
        this.masterTemplate = masterTemplate;
        this.slaveTemplate = slaveTemplate;
        this.objectMapper = objectMapper;
        this.rediSearchClient = rediSearchClient;
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

    public void setObjectOpsHash(@NonNull String sessionId, DataType dataType, Object o) {
        String redisKey = "sessionId:" + sessionId;

        // Redis에 객체 저장
        masterTemplate.opsForHash().put(redisKey, dataType.getType(), o);
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
    public long getExpired(@NonNull String key) {
        return slaveTemplate.getExpire(key);
    }

    /**
     * Redis expired TimeUnit 으로 변환한 값
     *
     * @param key      Redis 키
     * @param timeUnit timeUnit 으로 변환
     * @return 키가 존재하면 true, 존재하지 않으면 false
     */
    public long getExpiredByTimeUnit(@NonNull String key, TimeUnit timeUnit) {
        return slaveTemplate.getExpire(key, timeUnit);
    }

    /**
     * redis 에 저장된 데이터 key 를 특정 pattern 에 맞춰 가져옴
     *
     * @param pattern
     * @return pattern 에 맞는 key set
     */
    public Set<String> getKeysByPattern(String pattern) {
        Set<String> keys = new HashSet<>();

        // SCAN 옵션 설정 :: 와일드카드 검색할때는 뒤에 * 도 함께 붙여주자
        ScanOptions scanOptions = ScanOptions.scanOptions().match("*" + pattern + "*").count(100).build();

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
     *
     * @param key
     * @return
     */
    public int incrementUserCount(String key, String field, int count) {
        return masterTemplate.opsForHash().increment(key, field, count).intValue();
    }

    /**
     * redis 에 저장된 데이터를 가져와서 값을 count 만큼 감소시킨다
     *
     * @param key
     * @return
     */
    public int decrementUserCount(String key, String field, int count) {
        return masterTemplate.opsForHash().increment(key, field, -(count)).intValue();
    }

    public int getUserCount(String sessionId) {
        String redisKey = this.makeRedisKey(sessionId);
        Integer count = (Integer) slaveTemplate.opsForHash().get(redisKey, DataType.USER_COUNT.getType());
        return count == null ? 0 : count;
    }

    /**
     * user_count 값이 0인 키들을 검색한다
     *
     * @return List<String> - 값이 0인 키들의 목록
     */
    public List<String> getSessionListForDelete() throws BadRequestException {
        String pattern = "*sessionId:" + "*";
        List<String> sessionList = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();

        Cursor<byte[]> cursor = slaveTemplate.getConnectionFactory().getConnection().scan(options);

        while (cursor.hasNext()) {
            String key = new String(cursor.next()).replace("\"", "").replace("sessionId:", "");
            if (key.contains("userList")) {
                continue;
            }
            int userCount = this.getUserCount(key);
            if (0 == userCount) {
                sessionList.add(key);
                continue;
            }
            List userList = this.getRedisDataByDataType(key, DataType.USER_LIST, List.class);
            if (CollectionUtils.isEmpty(userList)) {
                sessionList.add(key);
            }
        }

        return sessionList;
    }

    /**
     * currentUserCount 가 maxUserCount 보다 큰 sessionId 들을 검색한다
     *
     * @return List<String> - currentUserCount > maxUserCount인 키들의 목록
     */
    public List<String> getSessionListForOverflow() throws BadRequestException {
        String pattern = "*sessionId:" + "*";
        List<String> sessionList = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();

        Cursor<byte[]> cursor = slaveTemplate.getConnectionFactory().getConnection().scan(options);

        while (cursor.hasNext()) {
            String key = new String(cursor.next()).replace("\"", "").replace("sessionId:", "");
            if (key.contains("userList")) {
                continue;
            }

            try {
                ChatRoomInVo chatRoomInVo = this.getRedisDataByDataType(key, DataType.CHATROOM, ChatRoomInVo.class);
                if (chatRoomInVo == null) {
                    continue;  // ChatRoomInVo가 null인 경우 다음 키로 넘어감
                }

                int maxUserCount = chatRoomInVo.getMaxUserCount();
                int currentUserCount = this.getUserCount(key);

                // currentUserCount 가 maxUserCount 보다 큰 경우만 목록에 추가
                // maxUserCount가 0인 경우는 유효하지 않은 설정으로 간주하여 제외
                if (currentUserCount > maxUserCount && maxUserCount > 0) {
                    sessionList.add(key);
                }
            } catch (Exception e) {
                // 예외 발생 시 로그 기록 및 계속 진행
                log.error("Error processing key: " + key, e);
            }
        }

        return sessionList;
    }

    /**
     * 레디스에 있는 전체 sessionId 를 가져와 현재 활성화된 sessionId 와 비교한다.
     *
     * @return List<String> - inactive 된 sessionId 목록
     */
    public List<String> getSessionListForInactive(List<String> activeSessionList) throws BadRequestException {
        String pattern = "sessionId:" + "*";
        List<String> sessionList = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();

        // 검색 성능 향상을 위해 activeSessionList를 HashSet으로 변환
        Set<String> activeSessionSet = new HashSet<>(activeSessionList);

        try {
            Cursor<byte[]> cursor = slaveTemplate.getConnectionFactory().getConnection().scan(options);

            while (cursor.hasNext()) {
                try {
                    String key = new String(cursor.next()).replace("\"", "").replace("sessionId:", "");
                    if (key.contains("userList")) {
                        continue;
                    }

                    // activeSessionSet에 포함되지 않은 sessionId만 추가
                    if (!activeSessionSet.contains(key)) {
                        sessionList.add(key);
                    }
                } catch (Exception e) {
                    // 개별 키 처리 중 오류 발생 시 로그 기록 후 계속 진행
                    log.error("Error processing key in getSessionListForInactive: {}", e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            // 전체 스캔 과정에서 오류 발생 시 로그 기록 후 예외 발생
            log.error("Error scanning Redis keys in getSessionListForInactive: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to get inactive session list: " + e.getMessage());
        }

        return sessionList;
    }

    public boolean deleteKeysByStr(String str) {
        String pattern = "*" + str + "*";
        try {
            // SCAN 명령어를 사용하여 키 검색 및 삭제
            ScanOptions scanOptions = ScanOptions.scanOptions().match(pattern).count(100).build();

            List<String> keysToDelete = new ArrayList<>();
            try (Cursor<byte[]> cursor = slaveTemplate.execute(
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
        } catch (RedisException e) {
            log.error("UnExcepted Redis Exception ::: {}", Arrays.toString(e.getStackTrace()));
            return false;
        }
    }

    public void createChatRoomJob(String sessionId, ChatRoomInVo chatRoomInVo, OpenViduDto openViduDto) {
        String redisKey = "sessionId:" + sessionId;
        // 채팅방 객체 저장
        masterTemplate.opsForHash().put(redisKey, DataType.CHATROOM.getType(), chatRoomInVo);
        masterTemplate.opsForHash().put(redisKey, "sessionId", sessionId);
        masterTemplate.opsForHash().put(redisKey, "creator", chatRoomInVo.getCreator());
        masterTemplate.opsForHash().put(redisKey, "roomName", chatRoomInVo.getRoomName());
        masterTemplate.opsForHash().put(redisKey, "createDate", chatRoomInVo.getCreateDate());
        // OpenVidu 객체 저장
        masterTemplate.opsForHash().put(redisKey, DataType.OPENVIDU.getType(), openViduDto);
    }

    public void joinUserJob(String sessionId, UserOutVo user, OpenViduDto openViduDto) {
        String redisKey = "sessionId:" + sessionId;
        // OpenVidu 객체 저장
        masterTemplate.opsForHash().put(redisKey, DataType.OPENVIDU.getType(), openViduDto);
        // 유저 수 증가
        this.incrementUserCount(redisKey, DataType.USER_COUNT.getType(), 1);
        // userList 에 추가
        String userListKey = redisKey + ":userList";
        // Set에 유저 추가
        masterTemplate.opsForSet().add(userListKey, user);
    }

    public void leftUserJob(String sessionId, OpenViduDto openViduDto, UserOutVo leftUser) {
        String redisKey = "sessionId:" + sessionId;

        // OpenVidu 객체 저장
        masterTemplate.opsForHash().put(redisKey, DataType.OPENVIDU.getType(), openViduDto);
        // 유저 수 감소
        this.decrementUserCount(redisKey, DataType.USER_COUNT.getType(), 1);
        // userList 에서 제거
        String userListKey = redisKey + ":userList";
        masterTemplate.opsForSet().remove(userListKey, leftUser);
    }

    public <T> T getRedisDataByDataType(String key, DataType dataType, Class<T> clazz) throws BadRequestException {
        String redisKey = "";
        if (DataType.LOGIN_USER.equals(dataType) || DataType.USER_REFRESH_TOKEN.equals(dataType)) {
            redisKey = key.contains("user:") ? key : "user:" + key;
        } else {
            redisKey = makeRedisKey(key);
        }
        switch (dataType) {
            case CHATROOM:
                return clazz.cast(slaveTemplate.opsForHash().get(redisKey, DataType.CHATROOM.getType()));
            case OPENVIDU:
                return clazz.cast(slaveTemplate.opsForHash().get(redisKey, DataType.OPENVIDU.getType()));
            case USER_LIST:
                String userListKey = redisKey + ":userList";
                return clazz.cast(slaveTemplate.opsForSet().members(userListKey).stream().collect(Collectors.toList()));
            case LOGIN_USER:
                return clazz.cast(slaveTemplate.opsForHash().get(redisKey, DataType.LOGIN_USER.getType()));
            case USER_REFRESH_TOKEN:
                return clazz.cast(slaveTemplate.opsForHash().get(redisKey, DataType.USER_REFRESH_TOKEN.getType()));
            default:
                throw new BadRequestException("Dose Not Exist DataType");
        }
    }

    @NotNull
    private String makeRedisKey(String sessionId) {
//        String redisKey;
//        if (sessionId.contains("sessionId:")) {
//            redisKey = sessionId;
//        } else {
//            redisKey = "sessionId:"+ sessionId;
//        }
        return sessionId.contains("sessionId:") ? sessionId : "sessionId:" + sessionId;
    }

    public Map<Object, Object> getAllChatRoomData(String sessionId) {
        String redisKey = this.makeRedisKey(sessionId);
        return slaveTemplate.opsForHash().entries(redisKey);
    }

    public void saveConnectionTokens(String sessionId, String userId, ConnectionOutVo cameraToken, ConnectionOutVo screenToken) {
        String redisKey = "sessionId:" + sessionId;

        // 유저별 토큰 정보 저장
        Map<String, ConnectionOutVo> putMap = new HashMap<>();
        putMap.put(DataType.redisDataTypeConnection(userId, DataType.CONNECTION_CAMERA), cameraToken);
        putMap.put(DataType.redisDataTypeConnection(userId, DataType.CONNECTION_SCREEN), screenToken);
        masterTemplate.opsForHash().putAll(redisKey, putMap);
    }

    public void deleteConnectionTokens(String sessionId, String userId) {
        String redisKey = "sessionId:" + sessionId;
        // 유저별 토큰 정보 제거
        masterTemplate.opsForHash().delete(redisKey, DataType.redisDataTypeConnection(userId, DataType.CONNECTION_CAMERA), DataType.redisDataTypeConnection(userId, DataType.CONNECTION_SCREEN));
    }

    public Map<String, ConnectionOutVo> getConnectionTokens(String sessionId, String userId) {
        // 유저별 토큰 정보 조회
        String redisKey = "sessionId:" + sessionId;
        Map<Object, Object> entries = slaveTemplate.opsForHash().entries(redisKey);
        Map<String, ConnectionOutVo> tokenMap = new HashMap<>();
        tokenMap.put("cameraToken", (ConnectionOutVo) entries.get(DataType.redisDataTypeConnection(userId, DataType.CONNECTION_CAMERA)));
        tokenMap.put("screenToken", (ConnectionOutVo) entries.get(DataType.redisDataTypeConnection(userId, DataType.CONNECTION_SCREEN)));

        return tokenMap;
    }

    public void addFavoriteRoom(String userId, String roomId) {
        String redisKey = userId + ":" + DataType.FAVORITES.getType();
        masterTemplate.opsForZSet().add(redisKey, roomId, new Date().getTime());
    }

    public void removeFavoriteRoom(String userId, String roomId) {
        String redisKey = userId + ":" + DataType.FAVORITES.getType();
        masterTemplate.opsForZSet().remove(redisKey, roomId);
    }

    public Set<String> getFavoriteRoomList(String userId) {
        String redisKey = userId + ":" + DataType.FAVORITES.getType();
        // Object 타입을 String 타입으로 변환
        Set<String> favoriteRooms = slaveTemplate.opsForZSet().reverseRange(redisKey, 0, -1).stream()
                .map(Object::toString)
                .collect(Collectors.toSet());

        return favoriteRooms;
    }

    public List<Document> searchByKeyword(RedisIndex redisIndex, String keyword, int pageNum, int pageSize) {
        // searchType 에 맞춰 indexName 을 가져옴
        RediSearch rediSearch = rediSearchClient.getRediSearch(redisIndex.getType());

        // Redis 검색 결과에서 openvidu 필드의 JSON 문자열 가져오기
//        int pageNumber = 0;  // 원하는 페이지 번호
//        int pageSize = 5;    // 한 페이지에 표시할 항목 수
        String queryParam = "*";
        SearchOptions searchOptions = null;
        // or 조건이 제대로 동작하려면 조건과 조건을 () 로 구분해서 묶어야함
        switch (redisIndex) {
            case CHATROOM:
                if (!StringUtil.isNullOrEmpty(keyword)) {
                    // 검색어가 있을 때: creator 또는 roomName 필드 검색, 그리고 user: 값 제외
                    queryParam = "((@creator:*" + keyword + "*) | (@roomName:*" + keyword + "*))";
                }
                searchOptions = new SearchOptions()
                        .page(pageNum * pageSize, pageSize)  // 페이지 설정
                        .returnFields("sessionId")  // sessionId 필드만 반환
                        .sort(new SortBy("createDate", SortOrder.DESC));  // createDate 기준 내림차순 정렬
                break;

            case LOGIN_USER:
                if (!StringUtil.isNullOrEmpty(keyword)) {
                    // 검색어가 있을 때: userId 또는 nickName 필드 검색
                    queryParam = "((@userId:*" + keyword + "*) | (@nickName:*" + keyword + "*))";
                }
                searchOptions = new SearchOptions()
                        .page(pageNum * pageSize, pageSize)  // 페이지 설정
                        .returnFields(DataType.LOGIN_USER.getType())
                        .sort(new SortBy("userId", SortOrder.DESC));  // userId 기준 내림차순 정렬
                break;
        }


        List<Document> documents = rediSearch.search(
                queryParam,
                searchOptions
        ).getDocuments();

        return documents;
    }

    public boolean searchDuplicateRoomName(String keyword) {
        // searchType 에 맞춰 indexName 을 가져옴
        RediSearch rediSearch = rediSearchClient.getRediSearch(RedisIndex.CHATROOM.getType());

        SearchOptions searchOptions = new SearchOptions()
                .returnFields("roomName");  // sessionId 필드만 반환

        long count = rediSearch.search(
                "@roomName:*" + keyword + "*",
                searchOptions
        ).getTotal();

        return count > 0;
    }

    public void saveLoginUser(UserOutVo user) {
        // redisKey = user:userIdx
        String redisKey = "user:" + user.getIdx();
        // index = userId && nickName
        masterTemplate.opsForHash().put(redisKey, DataType.LOGIN_USER.getType(), user);
        masterTemplate.opsForHash().put(redisKey, "userId", user.getId());
        masterTemplate.opsForHash().put(redisKey, "nickName", user.getNickName());
    }

    public void deleteLoginUser(Long userIdx) {
        String redisKey = "user:" + userIdx;
        masterTemplate.delete(redisKey);
    }

    public void saveRefreshToken(Long userIdx, String refreshToken) {
        // redisKey = user:userIdx
        String redisKey = "user:" + userIdx;
        // index = userId && nickName
        masterTemplate.opsForHash().put(redisKey, DataType.USER_REFRESH_TOKEN.getType(), refreshToken);
    }

    public void updateExpiredDate(Long userIdx){
        // redisKey = user:userIdx
        String redisKey = "user:" + userIdx;
        // refresh-token 을 갱신할때마다 시간 update
        masterTemplate.opsForHash().put(redisKey, "token_update_time", new Date().getTime());
        // 유저 데이터 유효시간 업데이트
        masterTemplate.expire(redisKey, REDIS_TIMEOUT, TimeUnit.DAYS);
    }

    public void deleteInactiveUsers() {
        Set<String> userKeys = slaveTemplate.keys("user:*");
        long currentTime = System.currentTimeMillis();
        long inactivityThreshold = 3 * 60 * 60 * 1000; // 3시간 (밀리초)

        if (!CollectionUtils.isEmpty(userKeys)) {
            // 삭제할 키를 필터링
            Set<String> deleteKeys = userKeys.stream()
                    .filter(key -> {
                        try {
                            // 토큰 마지막 갱신시간 확인 및 TTL 값 확인
                            Long lastUpdateTime = (Long) masterTemplate.opsForHash().get(key, "token_update_time");
                            Long ttl = masterTemplate.getExpire(key);

                            // 삭제 조건 확인
                            return (lastUpdateTime == null) ||
                                    (currentTime - lastUpdateTime >= inactivityThreshold) ||
                                    (ttl != null && ttl <= 0);
                        } catch (Exception e) {
                            // 예외 발생 시 로그 출력 및 키 삭제 대상으로 포함
                            log.error("키 확인 중 오류 발생: " + key + " - " + e.getMessage());
                            return true;
                        }
                    })
                    .collect(Collectors.toSet());

            // 삭제할 키가 존재하면 한 번에 삭제
            if (!deleteKeys.isEmpty()) {
                masterTemplate.delete(deleteKeys);
            }
        }
    }
}
