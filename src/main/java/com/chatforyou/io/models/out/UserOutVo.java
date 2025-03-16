package com.chatforyou.io.models.out;

import com.chatforyou.io.entity.Board;
import com.chatforyou.io.entity.ChatRoom;
import com.chatforyou.io.entity.SocialUser;
import com.chatforyou.io.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserOutVo implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long idx;
    private String id;
    private String pwd;
    private String name;
    private String nickName;
    private String provider;
    private List<User> friendList;
    private Long createDate;
    private Long lastLoginDate;

//    TODO 아래 기능들에 대해 논의 필요. 사용안하면 삭제 필요.
//    private Set<Board> boards;
//
//    private Set<ChatRoom> chatRooms;
//
//    private List<SocialUser> socialUsers;

    public static UserOutVo of(User user, boolean includePwd) {
        return UserOutVo.builder()
                .idx(user.getIdx())
                .id(user.getId())
                .pwd(includePwd ? user.getPwd() : null)
                .name(user.getName())
                .nickName(user.getNickName())
                .provider("")
                .lastLoginDate(user.getLastLoginDate())
                .build();
    }

    public static UserOutVo of(SocialUser socialUser, boolean includePwd) {
        User user = socialUser.getUser();
        return UserOutVo.builder()
                .idx(user.getIdx())
                .id(user.getId())
                .pwd(includePwd ? user.getPwd() : null)
                .name(user.getName())
                .nickName(user.getNickName())
                .provider(socialUser.getProvider())
                .lastLoginDate(user.getLastLoginDate())
                .build();
    }

//    public Set<Board> getBoards(User user) {
//        // TODO 서비스 로직 이용
//        if (this.boards == null) {
//            this.boards = user.getBoards();
//        }
//        return this.boards;
//    }

//    public Set<ChatRoom> getChatRooms(User user) {
//        // TODO 서비스 로직 이용
//        if (this.chatRooms == null) {
//            this.chatRooms = user.getChatRooms();
//        }
//        return this.chatRooms;
//    }

//    public List<SocialUser> getSocialUsers(User user) {
//        // TODO 서비스 로직 이용
//        if (this.socialUsers == null) {
//            this.socialUsers = user.getSocialUsers();
//        }
//        return this.socialUsers;
//    }
}
