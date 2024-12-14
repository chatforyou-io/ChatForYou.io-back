package com.chatforyou.io.models.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Date;
import java.util.Objects;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRoomInVo {
    private String sessionId;
    private Long userIdx;
    private String creator;
    private String roomName;
    private String pwd;
    private Boolean usePwd;
    private Boolean usePrivate;
    private Boolean useRtc;
    private String desc;
    private Integer maxUserCount;
    private Long createDate;
    private Long updateDate;

    public void setRequiredRoomInfo(String sessionId, String creator, Long createDate, Long updateDate){
        this.sessionId = sessionId;
        this.creator = creator;
        this.createDate = createDate;
        this.updateDate = updateDate;
    }

    public static ChatRoomInVo ofUpdate(ChatRoomInVo chatRoomInVo, ChatRoomInVo newChatRoomInVo){
        return ChatRoomInVo.builder()
                .sessionId(chatRoomInVo.getSessionId())
                .userIdx(chatRoomInVo.getUserIdx())
                .roomName(Objects.isNull(newChatRoomInVo.getRoomName()) ? chatRoomInVo.getRoomName() : newChatRoomInVo.getRoomName())
                .usePwd(Boolean.TRUE.equals(newChatRoomInVo.getUsePwd()))
                .pwd(Objects.isNull(newChatRoomInVo.getPwd()) ? chatRoomInVo.getPwd() : newChatRoomInVo.getPwd())
                .usePrivate(Boolean.TRUE.equals(newChatRoomInVo.getUsePrivate()))
                .maxUserCount(Objects.isNull(newChatRoomInVo.getMaxUserCount()) ? chatRoomInVo.getMaxUserCount() : newChatRoomInVo.getMaxUserCount())
                .desc(Objects.isNull(newChatRoomInVo.getDesc()) ? chatRoomInVo.getDesc() : newChatRoomInVo.getDesc())
                .createDate(chatRoomInVo.getCreateDate())
                .updateDate(new Date().getTime())
                .build();
    }
}
