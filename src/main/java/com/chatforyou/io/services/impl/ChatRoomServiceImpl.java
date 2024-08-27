package com.chatforyou.io.services.impl;

import ch.qos.logback.core.util.StringUtil;
import com.chatforyou.io.entity.ChatRoom;
import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.DataType;
import com.chatforyou.io.models.OpenViduData;
import com.chatforyou.io.models.ValidateType;
import com.chatforyou.io.models.in.ChatRoomInVo;
import com.chatforyou.io.models.out.ChatRoomOutVo;
import com.chatforyou.io.models.out.ConnectionOutVo;
import com.chatforyou.io.repository.ChatRoomRepository;
import com.chatforyou.io.repository.UserRepository;
import com.chatforyou.io.services.AuthService;
import com.chatforyou.io.services.ChatRoomService;
import com.chatforyou.io.services.OpenViduService;
import com.chatforyou.io.utils.RedisUtils;
import com.chatforyou.io.utils.ThreadUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomServiceImpl implements ChatRoomService {
    // TODO sessionID 에 인덱스 걸어두기

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final OpenViduService openViduService;
    private final RedisUtils redisUtils;

    @Override
    @Transactional // 에러가 발생할 시 rollback 될 수 있도록 @Transactional 사용
    public ChatRoomOutVo createChatRoom(ChatRoomInVo chatRoomInVo) throws BadRequestException {
        // 1. 데이터 검증
        checkChatRoomValidate(chatRoomInVo);

        User userEntity = userRepository.findUserByIdx(chatRoomInVo.getUserIdx())
                .orElseThrow(() -> new EntityNotFoundException("can not find user"));

        // 2. entity 로 변환
        ChatRoom chatRoomEntity = ChatRoom.of(chatRoomInVo, userEntity);

        // 4. 저장
        chatRoomRepository.saveAndFlush(chatRoomEntity);

        // 3. openvidu 방생성
        openViduService.createOpenViduRoom(chatRoomEntity);

        ChatRoomOutVo chatRoom = ChatRoomOutVo.of(chatRoomEntity, 0);

        // 5. 새로운 room 저장
        redisUtils.setObject(DataType.redisDataKey(chatRoom.getSessionId(), DataType.CHATROOM), chatRoom);

        return chatRoom;
    }

    @Override
    public OpenViduData getOpenviduDataBySessionId(String sessionId) {
        return redisUtils.getObject(DataType.redisDataKey(sessionId, DataType.OPENVIDU), OpenViduData.class);
    }

    @Override
    public List<ChatRoomOutVo> getChatRoomList() {
        List<ChatRoomOutVo> chatRoomList = new ArrayList<>();
        Set<String> keys = redisUtils.getKeysByPattern(DataType.redisDataKey("*", DataType.CHATROOM));
        for (String key : keys) {
            ChatRoomOutVo chatRoom = redisUtils.getObject(key.replace("\"", ""), ChatRoomOutVo.class);
            int currentUserCount = redisUtils.getObject(DataType.redisDataKey(chatRoom.getSessionId(), DataType.USER_COUNT), Integer.class);
            chatRoom.setCurrentUserCount(currentUserCount);
            if (chatRoom != null) {
                chatRoomList.add(chatRoom);
            }
        }

        return chatRoomList;
    }

    @Override
    public ChatRoomOutVo findChatRoomByRoomName(String roomName) {
        ChatRoom chatRoom = chatRoomRepository.findChatRoomByName(roomName)
                .orElseThrow(() -> new EntityNotFoundException("Can not find chatroom"));
        int currentUserCount = redisUtils.getObject(DataType.redisDataKey(chatRoom.getSessionId(), DataType.USER_COUNT), Integer.class);
        return ChatRoomOutVo.of(chatRoom, currentUserCount);
    }

    @Override
    public Map<String, String> joinChatRoom(String roomName, Long userIdx) throws BadRequestException {
        Map<String, String> tokens = new HashMap<>();
        ChatRoom chatRoomEntity = chatRoomRepository.findChatRoomByName(roomName)
                .orElseThrow(() -> new EntityNotFoundException("Can not find ChatRoom"));
        User joinUser = userRepository.findUserByIdx(userIdx)
                .orElseThrow(()-> new EntityNotFoundException("Can not find user"));

        String sessionId = chatRoomEntity.getSessionId();
        Long joinUserIdx = joinUser.getIdx();
        openViduService.joinOpenviduRoom(sessionId, joinUser);

        tokens.put("camera_token", openViduService.getConnection(sessionId, joinUserIdx, "camera").getToken());
        tokens.put("screen_token", openViduService.getConnection(sessionId, joinUserIdx, "screen").getToken());
        return tokens;
    }

    @Override
    public Map<String, Object> getConnectionInfo(String sessionId, Long userIdx) {
        userRepository.findUserByIdx(userIdx)
                .orElseThrow(() -> new EntityNotFoundException("can not find user"));
        Map<String, Object> result = new ConcurrentHashMap<>();
        ConnectionOutVo cameraToken = openViduService.getConnection(sessionId, userIdx, "camera");
        ConnectionOutVo screenToken = openViduService.getConnection(sessionId, userIdx, "screen");
        if (Objects.nonNull(screenToken) && Objects.nonNull(cameraToken)) {
            result.put("camera_token", cameraToken);
            result.put("screen_token", screenToken);
        } else {
            result.put("token_result", "session or token does not exist");
        }

        return result;
    }

    @Override
    public ChatRoomOutVo getChatRoomBySessionId(String sessionId) {
        ChatRoomOutVo chatRoomOutVo = redisUtils.getObject(DataType.redisDataKey(sessionId, DataType.CHATROOM), ChatRoomOutVo.class);
        int currentUserCount = redisUtils.getObject(DataType.redisDataKey(sessionId, DataType.USER_COUNT), Integer.class);
        chatRoomOutVo.setCurrentUserCount(currentUserCount);
        return chatRoomOutVo;
    }

    @Override
    public Boolean checkRoomPassword(String sessionId, String pwd) throws BadRequestException {
        ChatRoomOutVo chatRoom = redisUtils.getObject(DataType.redisDataKey(sessionId, DataType.CHATROOM), ChatRoomOutVo.class);
        if (Boolean.FALSE.equals(chatRoom.getUsePwd())) {
            throw new BadRequestException("This room does not require a password");
        }
        if (chatRoom.getPwd().equals(pwd)) {
            throw new BadRequestException("Invalid password");
        }
        return true;
    }

    @Override
    @Transactional
    public ChatRoomOutVo updateChatRoom(String sessionId, ChatRoomInVo chatRoomInVo) throws BadRequestException {
        ChatRoom chatRoomEntity = chatRoomRepository.findChatRoomBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Can not find ChatRoom"));

        ChatRoom newChatRoomEntity = ChatRoom.ofUpdate(chatRoomEntity, chatRoomInVo);
        chatRoomRepository.saveAndFlush(newChatRoomEntity);
        int currentUserCount = redisUtils.getObject(DataType.redisDataKey(sessionId, DataType.USER_COUNT), Integer.class);
        ChatRoomOutVo chatRoomOutVo = ChatRoomOutVo.of(newChatRoomEntity, currentUserCount);
        redisUtils.setObject(DataType.redisDataKey(sessionId, DataType.CHATROOM), chatRoomOutVo);

        return chatRoomOutVo;
    }

    @Override
    @Transactional
    public boolean deleteChatRoom(String sessionId) {
        chatRoomRepository.findChatRoomBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Can not find ChatRoom"));
        boolean result = chatRoomRepository.deleteChatRoomBySessionId(sessionId) == 1;
        if (!result) {
            log.error("Can not delete ChatRoom ==> sessionID :: {}", sessionId);
            throw new RuntimeException("Can not delete ChatRoom");
        }

        ThreadUtils.runTask(()-> redisUtils.deleteKeysBySessionId(sessionId), 10, 100, "delete All sessionInfo");

        return result;
    }

    private void checkChatRoomValidate(ChatRoomInVo chatRoomInVo) throws BadRequestException {
        if (StringUtil.isNullOrEmpty(chatRoomInVo.getRoomName())) {
            throw new BadRequestException("Required information is missing");
        }

        if (Objects.isNull(chatRoomInVo.getMaxUserCount()) || chatRoomInVo.getMaxUserCount() == 0) {
            throw new BadRequestException("User Count must be at least 2");
        }

        if (authService.validateStrByType(ValidateType.CHATROOM_NAME, chatRoomInVo.getRoomName())) {
            throw new BadRequestException("already exist room");
        }

    }
}
