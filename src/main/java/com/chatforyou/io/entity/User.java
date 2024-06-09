package com.chatforyou.io.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "USER")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDX", nullable = false)
    private Long idx;

    @Column(name = "ID", nullable = false, length = 50)
    private String id;

    @Column(name = "PWD", nullable = false, length = 100)
    private String pwd;

    @Column(name = "USE_PWD")
    private Boolean usePwd;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "NICK_NAME", length = 100)
    private String nickName;

    @Column(name = "CREATE_DATE", nullable = false)
    private Long createDate;

    @OneToMany(mappedBy = "userIdx", fetch = FetchType.LAZY)
    private Set<Board> boards;

    @OneToMany(mappedBy = "userIdx", fetch = FetchType.LAZY)
    private Set<Chatroom> chatRooms;

    @OneToMany(mappedBy = "userIdx", fetch = FetchType.LAZY)
    private List<Social> socials;

}