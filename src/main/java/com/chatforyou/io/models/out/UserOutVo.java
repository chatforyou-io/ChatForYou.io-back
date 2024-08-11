package com.chatforyou.io.models.out;

import com.chatforyou.io.entity.Board;
import com.chatforyou.io.entity.ChatRoom;
import com.chatforyou.io.entity.Social;
import com.chatforyou.io.entity.User;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class UserOutVo {

    private String id;

    private String pwd;

    private String name;

    private String nickName;

    private List<User> friendList;

    // TODO 아래 3가지는 모두 output 모델로 변경할 것
    private Set<Board> boards;

    private Set<ChatRoom> chatRooms;

    private List<Social> socials;

    public static UserOutVo of(User user) {
        return UserOutVo.builder()
                .id(user.getId())
                .pwd(user.getPwd())
                .name(user.getName())
                .nickName(user.getNickName())
                .build();
    }

    public Set<Board> getBoards(User user) {
        // TODO 서비스 로직 이용
        if (this.boards == null) {
            this.boards = user.getBoards();
        }
        return this.boards;
    }

    public Set<ChatRoom> getChatRooms(User user) {
        // TODO 서비스 로직 이용
        if (this.chatRooms == null) {
            this.chatRooms = user.getChatRooms();
        }
        return this.chatRooms;
    }

    public List<Social> getSocials(User user) {
        // TODO 서비스 로직 이용
        if (this.socials == null) {
            this.socials = user.getSocials();
        }
        return this.socials;
    }
}
