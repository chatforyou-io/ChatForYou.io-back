package com.chatforyou.io.entity;

import com.chatforyou.io.models.UserDto;
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