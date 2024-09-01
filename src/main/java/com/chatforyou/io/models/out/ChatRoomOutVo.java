package com.chatforyou.io.models.out;

import com.chatforyou.io.entity.ChatRoom;
import com.chatforyou.io.models.in.ChatRoomInVo;
import com.chatforyou.io.utils.RedisUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonIgnoreProperties({"pwd"})
public class ChatRoomOutVo {
    private Long userIdx;
    private String creator;
    private String sessionId;
    private String roomName;
    private String pwd;
    private Boolean usePwd;
    private Boolean usePrivate;
    private Boolean useRtc;
    @Setter
    private int currentUserCount;
    private Integer maxUserCount;
    @Setter
    private List<UserOutVo> userList;

    public static ChatRoomOutVo of(ChatRoom chatRoom, int currentUserCount){
        return ChatRoomOutVo.builder()
                .userIdx(chatRoom.getUser().getIdx())
                .creator(chatRoom.getUser().getNickName())
                .sessionId(chatRoom.getSessionId())
                .roomName(chatRoom.getName())
                .usePwd(chatRoom.getUsePwd())
                .usePrivate(chatRoom.getUsePrivate())
                .useRtc(chatRoom.getUseRtc())
                .currentUserCount(currentUserCount)
                .maxUserCount(chatRoom.getMaxUserCount())
                .build();
    }
}
