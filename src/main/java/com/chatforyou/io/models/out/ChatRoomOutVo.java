package com.chatforyou.io.models.out;

import com.chatforyou.io.entity.ChatRoom;
import com.chatforyou.io.models.in.ChatRoomInVo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonIgnoreProperties({"pwd"})
public class ChatRoomOutVo implements Serializable {
    private static final long serialVersionUID = 1L;
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
    private Long createDate;
    private Long updateDate;

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
                .createDate(chatRoom.getCreateDate())
                .updateDate(chatRoom.getUpdateDate())
                .build();
    }

    public static ChatRoomOutVo of(ChatRoomInVo chatRoomInVo, List<UserOutVo> list, int currentUserCount){
        return ChatRoomOutVo.builder()
                .sessionId(chatRoomInVo.getSessionId())
                .creator(chatRoomInVo.getCreator())
                .userIdx(chatRoomInVo.getUserIdx())
                .roomName(chatRoomInVo.getRoomName())
                .usePwd(chatRoomInVo.getUsePwd())
                .usePrivate(chatRoomInVo.getUsePrivate())
                .useRtc(chatRoomInVo.getUseRtc())
                .currentUserCount(currentUserCount)
                .maxUserCount(chatRoomInVo.getMaxUserCount())
                .userList(list)
                .createDate(chatRoomInVo.getCreateDate())
                .updateDate(chatRoomInVo.getUpdateDate())
                .build();
    }
}
