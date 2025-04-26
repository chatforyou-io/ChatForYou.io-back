package com.chatforyou.io.services;

import com.chatforyou.io.models.out.ChatRoomOutVo;
import com.chatforyou.io.models.out.UserOutVo;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.List;

public interface SseService {
    Flux<ServerSentEvent<?>> subscribeRoomList(Long userIdx);
    Flux<ServerSentEvent<?>> subscribeUserList(Long userIdx);
    Flux<ServerSentEvent<?>> subscribeRoomInfo(Long userIdx, String roomId);
    void notifyChatRoomList(List<ChatRoomOutVo> chatRoomList);
    void notifyChatRoomInfo(ChatRoomOutVo chatRoomInfo);
    void notifyUserList(List<UserOutVo> userList, List<UserOutVo> loginUserList);
}
