package com.chatforyou.io.services.impl;

import ch.qos.logback.core.util.StringUtil;
import com.chatforyou.io.client.OpenViduHttpException;
import com.chatforyou.io.client.OpenViduJavaClientException;
import com.chatforyou.io.entity.ChatRoom;
import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.DataType;
import com.chatforyou.io.models.OpenViduDto;
import com.chatforyou.io.models.ValidateType;
import com.chatforyou.io.models.in.ChatRoomInVo;
import com.chatforyou.io.models.out.ChatRoomOutVo;
import com.chatforyou.io.models.out.ConnectionOutVo;
import com.chatforyou.io.models.out.UserOutVo;
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
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomServiceImpl implements ChatRoomService {
    // TODO sessionID 에 인덱스 걸어두기
    // TODO 굳이 openvidu 에 connection 을 저장할 필요가 있는가??

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
        OpenViduDto openViduRoom = openViduService.createOpenViduRoom(chatRoomEntity);

        // 5. 새로운 room 저장
        ThreadUtils.runTask(() -> {
            try{
                chatRoomInVo.setSessionIdAndCreator(chatRoomEntity.getSessionId(), userEntity.getNickName());
                redisUtils.createChatRoomJob(chatRoomEntity.getSessionId(), chatRoomInVo, openViduRoom);
                return true;
            } catch (Exception e){
                log.error("Unknown Exception occurred :: {} : {}", e.getMessage(), e);
                return false;
            }
        }, 10, 10, "Create ChatRoom");

        return ChatRoomOutVo.of(chatRoomEntity, 0);
    }

    @Override
    public OpenViduDto getOpenviduDataBySessionId(String sessionId) throws BadRequestException {
        return redisUtils.getRedisDataByDataType(sessionId, DataType.OPENVIDU, OpenViduDto.class);
    }

    @Override
    public List<ChatRoomOutVo> getChatRoomList() throws BadRequestException {
        List<ChatRoomOutVo> chatRoomList = new ArrayList<>();
        List<String> keys = filterKeys(redisUtils.getKeysByPattern("sessionId:"));
        for (String key : keys) {
            key = key.replace("\"", "");
            Map<Object, Object> allChatRoomData = redisUtils.getAllChatRoomData(key);
            if (allChatRoomData.isEmpty() || allChatRoomData.get(DataType.CHATROOM.getType()) == null) {
                continue;
            }
            ChatRoomInVo chatRoom = (ChatRoomInVo) allChatRoomData.get(DataType.CHATROOM.getType());
            Integer currentUserCount = (Integer) allChatRoomData.get(DataType.USER_COUNT.getType());
            currentUserCount = currentUserCount == null ? 0 : currentUserCount;
            List userList = redisUtils.getRedisDataByDataType(key, DataType.USER_LIST, List.class);

            chatRoomList.add(ChatRoomOutVo.of(chatRoom, userList, currentUserCount));
        }

        return chatRoomList;
    }

    private List<String> filterKeys(Set<String> keys){
        return keys.stream()
                // userList가 포함되지 않은 키만 필터링
                .filter(key -> !key.contains("userList"))
                .collect(Collectors.toList());
    }

    @Override
    public ChatRoomOutVo findChatRoomByRoomName(String roomName) {
//        ChatRoomOutVo chatRoom = redisUtils.getRedisDataByDataType(sessionId, DataType.CHATROOM, ChatRoomOutVo.class);
//
//        if (Objects.isNull(chatRoom)) {
//            throw new BadRequestException("Can not find ChatRoom");
//        }
//        Set<UserOutVo> userList = redisUtils.getRedisDataByDataType(sessionId, DataType.USER_LIST, Set.class);
//        Integer currentUserCount = redisUtils.getRedisDataByDataType(sessionId, DataType.USER_COUNT, Integer.class);
//
//        chatRoom.setUserList(userList);
//        chatRoom.setCurrentUserCount(currentUserCount);

        return new ChatRoomOutVo();

    }

    @Override
    public Map<String, Object> joinChatRoom(String sessionId, Long userIdx) throws BadRequestException, OpenViduJavaClientException, OpenViduHttpException {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, String> tokens = new HashMap<>();

        ChatRoom chatRoomEntity = chatRoomRepository.findChatRoomBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Can not find ChatRoom"));
        User joinUser = userRepository.findUserByIdx(userIdx)
                .orElseThrow(() -> new EntityNotFoundException("Can not find user"));

//        ChatRoomInVo chatRoom = redisUtils.getRedisDataByDataType(sessionId, DataType.CHATROOM, ChatRoomInVo.class);
//        int userCount = redisUtils.getUserCount(sessionId)+1;
//
//        if (chatRoomEntity.getMaxUserCount() < userCount) {
//            throw new BadRequestException("Max User count");
//        }

        Map<Object, Object> allChatRoomData = redisUtils.getAllChatRoomData(sessionId);
        if (allChatRoomData.isEmpty() || allChatRoomData.get(DataType.CHATROOM.getType()) == null) {
            throw new BadRequestException("Can not find ChatRoom");
        }
        ChatRoomInVo chatRoom = (ChatRoomInVo) allChatRoomData.get(DataType.CHATROOM.getType());
        Integer currentUserCount = (Integer) allChatRoomData.get(DataType.USER_COUNT.getType());
        currentUserCount = currentUserCount == null ? 1 : currentUserCount+1;
        if (chatRoomEntity.getMaxUserCount() < currentUserCount) {
            throw new BadRequestException("Max User count");
        }

        OpenViduDto openViduDto = openViduService.joinOpenviduRoom(sessionId, joinUser);

        // Redis 저장을 thread 에서 하도록
        ThreadUtils.runTask(() -> {
            try {
                redisUtils.joinUserJob(sessionId, UserOutVo.of(joinUser, false), openViduDto);
                return true;
            } catch (Exception e) {
                log.error("Unknown Exception :: {} : {}", e.getMessage(), e);
                return false;
            }
        }, 10, 10, "Join User");


        List userList = redisUtils.getRedisDataByDataType(sessionId, DataType.USER_LIST, List.class);
        result.put("roomInfo", ChatRoomOutVo.of(chatRoom, userList, currentUserCount));
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
        ChatRoomOutVo chatRoom = redisUtils.getRedisDataByDataType(sessionId, DataType.CHATROOM, ChatRoomOutVo.class);
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
        int currentUserCount = redisUtils.getUserCount(sessionId);
        ChatRoomOutVo chatRoomOutVo = ChatRoomOutVo.of(newChatRoomEntity, currentUserCount);
        redisUtils.setObjectOpsHash(sessionId, DataType.CHATROOM, chatRoomOutVo);

        return chatRoomOutVo;
    }

    @Override
    @Transactional
    public boolean deleteChatRoom(String sessionId) {
        if (chatRoomRepository.findChatRoomBySessionId(sessionId).isPresent()) {
            boolean result = chatRoomRepository.deleteChatRoomBySessionId(sessionId) == 1;
            if (!result) {
                log.error("Can not delete ChatRoom ==> sessionID :: {}", sessionId);
                throw new RuntimeException("Can not delete ChatRoom");
            }
        } else {
            log.info("===== Already Deleted ChatRoom ====");
        }

        ThreadUtils.runTask(() -> redisUtils.deleteKeysByKey(sessionId), 10, 100, "Delete sessionInfo ");
        ThreadUtils.runTask(() -> openViduService.closeSession(sessionId), 10, 100, "Delete openvidu data ");
        return true;
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
