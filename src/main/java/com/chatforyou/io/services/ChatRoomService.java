package com.chatforyou.io.services;

import com.chatforyou.io.client.OpenViduHttpException;
import com.chatforyou.io.client.OpenViduJavaClientException;
import com.chatforyou.io.models.OpenViduDto;
import com.chatforyou.io.models.in.ChatRoomInVo;
import com.chatforyou.io.models.out.ChatRoomOutVo;
import org.apache.coyote.BadRequestException;

import java.util.List;
import java.util.Map;

public interface ChatRoomService {
    ChatRoomOutVo createChatRoom(ChatRoomInVo chatRoomInVo) throws BadRequestException;
    OpenViduDto getOpenviduDataBySessionId(String sessionId) throws BadRequestException;
    List<ChatRoomOutVo> getChatRoomList(String keyword, int pageNum, int pageSize) throws BadRequestException;
    Map<String, Object> joinChatRoom(String sessionId, Long userIdx) throws BadRequestException, OpenViduJavaClientException, OpenViduHttpException;
    Map<String, Object> getConnectionInfo(String sessionId, Long userId);
    ChatRoomOutVo findChatRoomBySessionId(String sessionId) throws BadRequestException;
    Boolean checkRoomPassword(String sessionId, String pwd) throws BadRequestException;
    ChatRoomOutVo updateChatRoom(String sessionId, ChatRoomInVo chatRoomInVo) throws BadRequestException;
    boolean deleteChatRoom(String sessionId) throws OpenViduJavaClientException, OpenViduHttpException;
}
