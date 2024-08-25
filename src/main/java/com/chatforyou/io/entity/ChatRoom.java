package com.chatforyou.io.entity;

import com.chatforyou.io.models.in.ChatRoomInVo;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "CHATROOM")
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDX", nullable = false)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_IDX", nullable = false)
    private User user;

    @Column(name="SESSION_ID", nullable = false, length = 40)
    private String sessionId;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "PWD", length = 100)
    private String pwd;

    @Column(name = "USE_PWD")
    private Boolean usePwd;

    @Column(name = "USE_PRIVATE")
    private Boolean usePrivate;

    @Column(name = "USE_RTC")
    private Boolean useRtc;

    @Column(name = "DESCRIPTION", length = 100)
    private String desc;

    @Column(name = "MAX_USER_COUNT", nullable = false)
    private Integer maxUserCount;

    @Column(name = "CREATE_DATE", nullable = false, updatable = false)
    private Long createDate;

    // TODO 삭제 필요한지 확인
    @Deprecated
    @OneToMany(mappedBy = "chatRoomIdx", fetch = FetchType.LAZY)
    private Set<OpenViduInfo> openViduInfos;

    public static ChatRoom of(ChatRoomInVo chatRoomInVo, User user){
        return ChatRoom.builder()
                .user(user)
                .sessionId(UUID.randomUUID().toString())
                .name(chatRoomInVo.getRoomName())
                .pwd(chatRoomInVo.getPwd())
                .usePwd(Objects.isNull(chatRoomInVo.getUsePwd()) ? false : chatRoomInVo.getUsePwd())
                .usePrivate(Objects.isNull(chatRoomInVo.getUsePrivate()) ? false : chatRoomInVo.getUsePrivate())
                .useRtc(Objects.isNull(chatRoomInVo.getUseRtc()) ? false : chatRoomInVo.getUseRtc())
                .desc(chatRoomInVo.getDesc())
                .maxUserCount(chatRoomInVo.getMaxUserCount())
                .createDate(new Date().getTime())
                .build();
    }

    private static String createSessionId() {
        return UUID.randomUUID().toString();
    }

}