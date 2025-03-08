package com.chatforyou.io.services;

import com.chatforyou.io.models.out.ChatRoomOutVo;
import org.apache.coyote.BadRequestException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface SseService {
    SseEmitter subscribeRoomList(Long userIdx) throws BadRequestException;
    SseEmitter subscribeRoomInfo(Long userIdx, String sessionId) throws BadRequestException;
    void notifyChatRoomList(List<ChatRoomOutVo> chatRoomList) throws BadRequestException;
    void notifyChatRoomInfo(ChatRoomOutVo chatRoomInfo);
}
