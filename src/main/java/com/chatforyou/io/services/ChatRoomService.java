package com.chatforyou.io.services;

import com.chatforyou.io.models.OpenViduData;
import com.chatforyou.io.models.in.ChatRoomInVo;
import com.chatforyou.io.models.out.ChatRoomOutVo;
import com.chatforyou.io.models.out.ConnectionOutVo;
import org.apache.coyote.BadRequestException;

import java.util.List;
import java.util.Map;

public interface ChatRoomService {
    ChatRoomOutVo createChatRoom(ChatRoomInVo chatRoomInVo) throws BadRequestException;
    OpenViduData getOpenviduDataBySessionId(String sessionId);
    List<ChatRoomOutVo> getChatRoomList();
    ChatRoomOutVo findChatRoomByRoomName(String roomName);
    Map<String, String> joinChatRoom(String roomName, Long userIdx) throws BadRequestException;
    Map<String, ConnectionOutVo> getConnectionInfo(String sessionId, Long userId);
    ChatRoomOutVo getChatRoomBySessionId(String sessionId);
    Boolean checkRoomPassword(String sessionId, String pwd) throws BadRequestException;
}
