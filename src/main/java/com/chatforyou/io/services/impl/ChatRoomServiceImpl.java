package com.chatforyou.io.services.impl;

import ch.qos.logback.core.util.StringUtil;
import com.chatforyou.io.client.OpenViduHttpException;
import com.chatforyou.io.client.OpenViduJavaClientException;
import com.chatforyou.io.entity.ChatRoom;
import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.*;
import com.chatforyou.io.models.in.ChatRoomInVo;
import com.chatforyou.io.models.out.ChatRoomOutVo;
import com.chatforyou.io.models.out.ConnectionOutVo;
import com.chatforyou.io.models.out.UserOutVo;
import com.chatforyou.io.repository.UserRepository;
import com.chatforyou.io.services.ChatRoomService;
import com.chatforyou.io.services.OpenViduService;
import com.chatforyou.io.services.SseService;
import com.chatforyou.io.utils.AuthUtils;
import com.chatforyou.io.utils.RedisUtils;
import com.chatforyou.io.utils.ThreadUtils;
import io.github.dengliming.redismodule.redisearch.index.Document;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomServiceImpl implements ChatRoomService {

    private final UserRepository userRepository;
    private final OpenViduService openViduService;
    private final RedisUtils redisUtils;
    private final AuthUtils authUtils;
    private final SseService sseService;

    @Override
    public ChatRoomOutVo createChatRoom(ChatRoomInVo chatRoomInVo, JwtPayload jwtPayload) throws BadRequestException {
        // 1. 데이터 검증
        checkChatRoomValidate(chatRoomInVo);

        // 1-2 토큰 검증
        if (!Objects.equals(chatRoomInVo.getUserIdx(), jwtPayload.getIdx())) {
            throw new BadRequestException("The user ID in the token does not match the user ID provided in the chat room information.");
        }

        User userEntity = userRepository.findUserByIdx(chatRoomInVo.getUserIdx())
                .orElseThrow(() -> new EntityNotFoundException("can not find user"));

        // 중복 방 이름 확인
        if (redisUtils.searchDuplicateRoomName(chatRoomInVo.getRoomName())) {
            // 예외처리
            throw new BadRequestException("Same RoomName Already Exist");
        }

        // 2. entity 로 변환
        ChatRoom chatRoomEntity = ChatRoom.of(chatRoomInVo, userEntity);

        // 3. openvidu 방생성
        OpenViduDto openViduRoom = openViduService.createOpenViduRoom(chatRoomEntity);

        // 5. 새로운 room 저장
        ThreadUtils.executeAsyncTask(
                // Redis 작업
                () -> {
                    try {
                        chatRoomInVo.setRequiredRoomInfo(chatRoomEntity.getSessionId(), userEntity.getNickName(),
                                chatRoomEntity.getCreateDate(), chatRoomEntity.getUpdateDate());
                        redisUtils.createChatRoomJob(chatRoomEntity.getSessionId(), chatRoomInVo, openViduRoom);
                        return true;
                    } catch (Exception e) {
                        log.error("Unknown Exception occurred :: {} : {}", e.getMessage(), e.getMessage());
                        return false;
                    }
                },
                10, 10, "Create ChatRoom",
                // 성공 시 후속 작업
                result -> {
                    if(Boolean.TRUE.equals(result)) {
                        try {
                            sseService.notifyChatRoomList(this.getChatRoomList("", 0, 9));
                        } catch (Exception e) {
                            log.error("Unknown Runtime Exception | Message ID: {}, Details: {}", e.getMessage(), e.getStackTrace());
                        }
                    }
                }
        );

        return ChatRoomOutVo.of(chatRoomEntity, 0);
    }

    @Override
    public OpenViduDto getOpenviduDataBySessionId(String sessionId) throws BadRequestException {
        return redisUtils.getRedisDataByDataType(sessionId, DataType.OPENVIDU, OpenViduDto.class);
    }

    @Override
    public List<ChatRoomOutVo> getChatRoomList(String keyword, int pageNum, int pageSize) throws BadRequestException {
        List<ChatRoomOutVo> chatRoomList = new ArrayList<>();
        pageNum = pageNum !=0 ? pageNum - 1 : pageNum;
        List<Document> roomList = redisUtils.searchByKeyword(RedisIndex.CHATROOM, keyword, pageNum, pageSize);
        for (Document document : roomList) {
            if (document.getFields().get("sessionId") == null) {
                continue;
            }
            String sessionId = document.getFields().get("sessionId").toString().replace("\"", "");
            Map<Object, Object> allChatRoomData = redisUtils.getAllChatRoomData(sessionId);
            if (allChatRoomData.isEmpty() || allChatRoomData.get(DataType.CHATROOM.getType()) == null) {
                continue;
            }
            ChatRoomInVo chatRoom = (ChatRoomInVo) allChatRoomData.get(DataType.CHATROOM.getType());
            Integer currentUserCount = (Integer) allChatRoomData.get(DataType.USER_COUNT.getType());
            currentUserCount = currentUserCount == null ? 0 : currentUserCount;
            List userList = redisUtils.getRedisDataByDataType(sessionId, DataType.USER_LIST, List.class);

            chatRoomList.add(ChatRoomOutVo.of(chatRoom, userList, currentUserCount));
        }
        return chatRoomList;
    }

    @Override
    public Map<String, Object> joinChatRoom(String sessionId, Long userIdx, JwtPayload jwtPayload) throws BadRequestException, OpenViduJavaClientException, OpenViduHttpException {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, String> tokens = new HashMap<>();

        User joinUser = userRepository.findUserByIdx(userIdx)
                .orElseThrow(() -> new EntityNotFoundException("Can not find user"));

        // 1-2 토큰 검증
        if (!Objects.equals(userIdx, jwtPayload.getIdx())) {
            throw new BadRequestException("The user ID in the token does not match the user ID provided in the chat room information.");
        }

        // redis 에서 chatroom 정보 확인
        ChatRoomInVo chatRoom = redisUtils.getRedisDataByDataType(sessionId, DataType.CHATROOM, ChatRoomInVo.class);
        if (chatRoom == null) {
            throw new BadRequestException("Can not find ChatRoom");
        }
        // redis 에서 현재 유저 count 확인
        int currentUserCount = redisUtils.getUserCount(sessionId) + 1;
        Integer maxUserCount = chatRoom.getMaxUserCount();
        if (maxUserCount < currentUserCount) {
            throw new BadRequestException("Max User count");
        }

        OpenViduDto openViduDto = openViduService.joinOpenviduRoom(sessionId, joinUser);
        List userList = redisUtils.getRedisDataByDataType(sessionId, DataType.USER_LIST, List.class);
        ChatRoomOutVo roomInfo = ChatRoomOutVo.of(chatRoom, userList, currentUserCount);

        // 참여 유저 정보 update
        ThreadUtils.executeAsyncTask(
                // Redis 작업
                () -> {
                    try {
                        redisUtils.joinUserJob(sessionId, UserOutVo.of(joinUser, false), openViduDto);
                        return true;
                    } catch (Exception e) {
                        log.error("Redis 작업 중 예상치 못한 오류 발생 | Session ID: {}, Details: {}",
                                sessionId, e.getStackTrace());
                        return false;
                    }
                },
                10, 10, "Join User",
                // 성공 시 후속 작업
                jobResult -> {
                    if (Boolean.TRUE.equals(jobResult)) {
                        try {
                            sseService.notifyChatRoomInfo(roomInfo);
                            sseService.notifyChatRoomList(this.getChatRoomList("", 0, 9));
                        } catch (Exception e) {
                            log.error("Unknown Runtime Exception | Message ID: {}, Details: {}", e.getMessage(), e.getStackTrace());
                        }
                    }
                }
        );


        result.put("roomInfo", roomInfo);
        tokens.put("camera_token", openViduDto.getSession().getConnections().get("con_camera_"+userIdx).getToken());
        tokens.put("screen_token", openViduDto.getSession().getConnections().get("con_screen_"+userIdx).getToken());
        result.put("joinUserInfo", tokens);
        return result;
    }

    @Override
    public Map<String, Object> getConnectionInfo(String sessionId, Long userIdx) {
        userRepository.findUserByIdx(userIdx)
                .orElseThrow(() -> new EntityNotFoundException("can not find user"));
        Map<String, Object> result = new ConcurrentHashMap<>();
        Map<String, ConnectionOutVo> connection = openViduService.getConnection(sessionId, userIdx);
        ConnectionOutVo cameraConnection = connection.get("cameraToken");
        ConnectionOutVo screenConnection = connection.get("screenToken");
        if (Objects.nonNull(cameraConnection) && Objects.nonNull(screenConnection)) {
            result.put("camera_token", cameraConnection.getToken());
            result.put("screen_token", screenConnection.getToken());
        } else {
            result.put("token_result", "session or token does not exist");
        }

        return result;
    }

    @Override
    public ChatRoomOutVo findChatRoomBySessionId(String sessionId) throws BadRequestException {
        Map<Object, Object> allChatRoomData = redisUtils.getAllChatRoomData(sessionId);
        if (allChatRoomData.isEmpty() || allChatRoomData.get(DataType.CHATROOM.getType()) == null) {
            throw new BadRequestException("Can not find ChatRoom");
        }
        ChatRoomInVo chatRoom = (ChatRoomInVo) allChatRoomData.get(DataType.CHATROOM.getType());
        Integer currentUserCount = (Integer) allChatRoomData.get(DataType.USER_COUNT.getType());
        currentUserCount = currentUserCount == null ? 0 : currentUserCount;
        List userList = redisUtils.getRedisDataByDataType(sessionId, DataType.USER_LIST, List.class);

        return ChatRoomOutVo.of(chatRoom, userList, currentUserCount);
    }

    @Override
    public Boolean checkRoomPassword(String sessionId, String pwd) throws BadRequestException {
        ChatRoomInVo chatRoom = redisUtils.getRedisDataByDataType(sessionId, DataType.CHATROOM, ChatRoomInVo.class);
        if (Objects.isNull(pwd)) {
            throw new BadRequestException("This room require a password");
        }
        if (!chatRoom.getPwd().equals(pwd)) {
            throw new BadRequestException("Invalid password");
        }
        return true;
    }

    @Override
    public ChatRoomOutVo updateChatRoom(String sessionId, ChatRoomInVo newChatRoomInVo, JwtPayload jwtPayload) throws BadRequestException {

        ChatRoomInVo redisChatRoom = redisUtils.getRedisDataByDataType(sessionId, DataType.CHATROOM, ChatRoomInVo.class);
        if (!Objects.equals(redisChatRoom.getUserIdx(), jwtPayload.getIdx())) {
            throw new BadRequestException("The user ID in the token does not match the user ID provided in the chat room information.");
        }

        ChatRoomInVo chatRoomInVo = ChatRoomInVo.ofUpdate(redisChatRoom, newChatRoomInVo);
        redisUtils.setObjectOpsHash(sessionId, DataType.CHATROOM, chatRoomInVo);

        return ChatRoomOutVo.of(chatRoomInVo);
    }

    @Override
    public boolean deleteChatRoom(String sessionId, JwtPayload jwtPayload, boolean isSystem) throws BadRequestException {
        if (!isSystem) {
            ChatRoomInVo redisChatRoom = redisUtils.getRedisDataByDataType(sessionId, DataType.CHATROOM, ChatRoomInVo.class);
            if (!Objects.equals(redisChatRoom.getUserIdx(), jwtPayload.getIdx())) {
                throw new BadRequestException("The user ID in the token does not match the user ID provided in the chat room information.");
            }
        }

        ThreadUtils.executeAsyncTask(
                // Redis 작업
                () -> {
                    openViduService.closeSession(sessionId);
                    redisUtils.deleteKeysByStr(sessionId);
                    return true;
                },
                10, 100, "Delete sessionInfo",
                // 성공 시 후속 작업
                jobResult -> {
                    try {
                        if (Boolean.TRUE.equals(jobResult)) {
                            sseService.notifyChatRoomList(this.getChatRoomList("", 0, 9));
                        }
                    } catch (BadRequestException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        return true;
    }

    private void checkChatRoomValidate(ChatRoomInVo chatRoomInVo) throws BadRequestException {
        if (StringUtil.isNullOrEmpty(chatRoomInVo.getRoomName())) {
            throw new BadRequestException("Required information is missing");
        }

        if (Objects.isNull(chatRoomInVo.getMaxUserCount()) || chatRoomInVo.getMaxUserCount() == 0) {
            throw new BadRequestException("User Count must be at least 2");
        }

        if (authUtils.validateStrByType(ValidateType.CHATROOM_NAME, chatRoomInVo.getRoomName())) {
            throw new BadRequestException("already exist room");
        }
    }
}
