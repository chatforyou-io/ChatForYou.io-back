package com.chatforyou.io.entity;

import com.chatforyou.io.models.in.UserInVo;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
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

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<ChatRoom> chatRooms;

    @OneToMany(mappedBy = "userIdx", fetch = FetchType.LAZY)
    private List<Social> socials;

    public static User ofSave(UserInVo userInVO){
        return User.builder()
                .idx(userInVO.getIdx())
                .id(userInVO.getId())
                .pwd(userInVO.getPwd())
                .usePwd(userInVO.getUsePwd())
                .name(userInVO.getName())
                .nickName(userInVO.getNickName())
                .createDate(new Date().getTime())
                .build();
    }

    public static User ofUpdate(UserInVo userInVO, User user){
        return User.builder()
                .idx(user.getIdx())
                .id(user.getId())
                .pwd(userInVO.getPwd())
                .usePwd(user.getUsePwd())
                .name(user.getName())
                .nickName(userInVO.getNickName())
                .createDate(user.getCreateDate())
                .build();
    }
}