package com.chatforyou.io.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "OPENVIDU_INFO")
public class OpenViduInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDX", nullable = false)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CHATROOM_IDX", nullable = false)
    private Chatroom chatroomIdx;

    @Column(name = "FIELD", nullable = false, length = 100)
    private String field;

}