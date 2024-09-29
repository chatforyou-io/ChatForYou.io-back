package com.chatforyou.io.models.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

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

    public void setSessionIdAndCreator(String sessionId, String creator){
        this.sessionId = sessionId;
        this.creator = creator;
    }
}
