package com.chatforyou.io.models.in;

import lombok.*;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@ToString
public class ChatRoomInVo {
    private Long userIdx;
    private String roomName;
    private String pwd;
    private Boolean usePwd;
    private Boolean usePrivate;
    private Boolean useRtc;
    private String desc;
    private Integer maxUserCount;

}
