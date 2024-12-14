package com.chatforyou.io.services.impl;

import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.JwtPayload;
import com.chatforyou.io.models.in.ChatRoomInVo;
import com.chatforyou.io.models.in.UserInVo;
import com.chatforyou.io.models.out.ChatRoomOutVo;
import com.chatforyou.io.models.out.UserOutVo;
import com.chatforyou.io.repository.ChatRoomRepository;
import com.chatforyou.io.repository.UserRepository;
import com.chatforyou.io.services.AuthService;
import com.chatforyou.io.services.ChatRoomService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@SpringBootTest
@Transactional
class ChatRoomServiceImplTest {
    Logger logger = LoggerFactory.getLogger(ChatRoomServiceImplTest.class);

    @Autowired
    private ChatRoomService chatRoomService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthService authService;


    @Test
    @DisplayName("방 저장 테스트 - 방 idx != token idx")
    void createChatRoomForError() throws BadRequestException {
        ChatRoomInVo chatRoomInVo = ChatRoomInVo.builder()
                .userIdx(22L)
                .roomName("test code 방")
                .usePwd(false)
                .usePrivate(true)
                .useRtc(true)
                .maxUserCount(2)
                .build();
        UserOutVo loginUserInfo = authService.getLoginUserInfo("sejon@test.com", "dGVzdDEyMzQhQA==");
        ChatRoomOutVo chatRoom = chatRoomService.createChatRoom(chatRoomInVo, JwtPayload.of(loginUserInfo));
        logger.info(chatRoom.toString());

    }

    @Test
    @DisplayName("방 저장 테스트 - 방 idx == token idx")
    void createChatRoom() throws BadRequestException {
        ChatRoomInVo chatRoomInVo = ChatRoomInVo.builder()
                .userIdx(32L)
                .roomName("test code 방")
                .usePwd(false)
                .usePrivate(true)
                .useRtc(true)
                .maxUserCount(2)
                .build();
        UserOutVo loginUserInfo = authService.getLoginUserInfo("sejon@test.com", "dGVzdDEyMzQhQA==");
        ChatRoomOutVo chatRoom = chatRoomService.createChatRoom(chatRoomInVo, JwtPayload.of(loginUserInfo));
        logger.info(chatRoom.toString());

    }
}