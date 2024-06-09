package com.chatforyou.io.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "CHATROOM")
public class Chatroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDX", nullable = false)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_IDX", nullable = false)
    private User userIdx;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "PWD", nullable = false, length = 100)
    private String pwd;

    @Column(name = "USE_PWD")
    private Boolean usePwd;

    @Column(name = "TYPE", nullable = false, length = 20)
    private String type;

    @Column(name = "MAX_USER_COUNT")
    private Integer maxUserCount;

    @Column(name = "CREATE_DATE", nullable = false)
    private Long createDate;

    @OneToMany(mappedBy = "chatroomIdx", fetch = FetchType.LAZY)
    private Set<OpenViduInfo> openViduInfos;

}