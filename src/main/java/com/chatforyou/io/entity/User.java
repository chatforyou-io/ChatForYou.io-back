package com.chatforyou.io.entity;

import com.chatforyou.io.models.UserDto;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
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

    private User(Long idx, String id, String pwd, Boolean usePwd, String name, String nickName, Long createDate, Set<Board> boards, Set<Chatroom> chatRooms, List<Social> socials) {
        this.idx = idx;
        this.id = id;
        this.pwd = pwd;
        this.usePwd = usePwd;
        this.name = name;
        this.nickName = nickName;
        this.createDate = createDate;
        this.boards = boards;
        this.chatRooms = chatRooms;
        this.socials = socials;
    }

    public static User of(UserDto userDto){
        return User.builder()
                .id(userDto.getId())
                .pwd(userDto.getPwd())
                .usePwd(userDto.getUsePwd())
                .name(userDto.getName())
                .nickName(userDto.getNickName())
                .createDate(userDto.getCreateDate())
                .build();
    }
}