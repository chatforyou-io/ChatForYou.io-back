package com.chatforyou.io.entity;

import ch.qos.logback.core.util.StringUtil;
import com.chatforyou.io.models.in.SocialUserInVo;
import com.chatforyou.io.models.in.UserInVo;
import com.chatforyou.io.models.in.UserUpdateVo;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

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

//    TODO 아래 기능들에 대해 논의 필요. 사용안하면 삭제 필요.
//    @OneToMany(mappedBy = "userIdx", fetch = FetchType.LAZY)
//    private Set<Board> boards;

//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
//    private Set<ChatRoom> chatRooms;
//
//    @OneToMany(mappedBy = "userIdx", fetch = FetchType.LAZY)
//    private List<SocialUser> socialUsers;

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

    public static User ofUpdate(UserUpdateVo userUpdateVo, User user){
        return User.builder()
                .idx(user.getIdx())
                .id(user.getId())
                .pwd(user.getPwd())
                .usePwd(user.getUsePwd())
                .name(user.getName())
                .nickName(StringUtil.isNullOrEmpty(userUpdateVo.getNickName()) ? user.getNickName() : userUpdateVo.getNickName())
                .createDate(user.getCreateDate())
                .build();
    }

    public static User ofSocialUser(SocialUserInVo socialUser){
        return User.builder()
                .id(socialUser.getId())
                .pwd(Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes()).toString())
                .usePwd(false)
                .name(socialUser.getName())
                .nickName(socialUser.getName())
                .createDate(new Date().getTime())
                .build();
    }
}