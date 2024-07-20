package com.chatforyou.io.entity;

import com.chatforyou.io.models.in.UserVO;
import com.chatforyou.io.models.out.UserInfo;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
    private Set<ChatRoom> chatRooms;

    @OneToMany(mappedBy = "userIdx", fetch = FetchType.LAZY)
    private List<Social> socials;

    public static User of(UserInfo userInfo){
        return User.builder()
                .id(userInfo.getId())
                .pwd(userInfo.getPwd())
                .usePwd(userInfo.getUsePwd())
                .name(userInfo.getName())
                .nickName(userInfo.getNickName())
                .createDate(userInfo.getCreateDate())
                .build();
    }

    public static User of(UserVO userVO){
        return User.builder()
                .idx(userVO.getIdx())
                .id(userVO.getId())
                .pwd(userVO.getPwd())
                .usePwd(userVO.getUsePwd())
                .name(userVO.getName())
                .nickName(userVO.getNickName())
                .build();
    }
}