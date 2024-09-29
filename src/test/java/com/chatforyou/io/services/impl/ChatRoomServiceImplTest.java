package com.chatforyou.io.services.impl;

import com.chatforyou.io.models.in.ChatRoomInVo;
import com.chatforyou.io.models.out.ChatRoomOutVo;
import com.chatforyou.io.repository.ChatRoomRepository;
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

@SpringBootTest
@Transactional
class ChatRoomServiceImplTest {
    Logger logger = LoggerFactory.getLogger(ChatRoomServiceImplTest.class);

    @Autowired
    private ChatRoomService chatRoomService;
    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("방 저장 테스트")
    void createChatRoom() throws BadRequestException {
        ChatRoomInVo chatRoomInVo = ChatRoomInVo.builder()
                .userIdx(22L)
                .roomName("test방")
                .usePwd(false)
                .usePrivate(true)
                .useRtc(true)
                .maxUserCount(2)
                .build();

        ChatRoomOutVo chatRoom = chatRoomService.createChatRoom(chatRoomInVo);
        logger.info(chatRoom.toString());

    }
}