package com.chatforyou.io.models;

import com.chatforyou.io.entity.Board;
import com.chatforyou.io.entity.Chatroom;
import com.chatforyou.io.entity.Social;
import com.chatforyou.io.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
public class UserDto {
    private Long idx;

    private String id;

    private String pwd;

    private Boolean usePwd;

    private String name;

    private String nickName;

    private Long createDate;

    private Set<Board> boards;

    private Set<Chatroom> chatRooms;

    private List<Social> socials;


    private UserDto(Long idx, String id, String pwd, Boolean usePwd, String name, String nickName, Long createDate, Set<Board> boards, Set<Chatroom> chatRooms, List<Social> socials) {
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

    public static UserDto of(User user) {
        return UserDto.builder()
                .idx(user.getIdx())
                .id(user.getId())
                .pwd(user.getPwd())
                .usePwd(user.getUsePwd())
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

    public Set<Chatroom> getChatRooms(User user) {
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
