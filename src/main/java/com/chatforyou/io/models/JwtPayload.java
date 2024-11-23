package com.chatforyou.io.models;

import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.out.UserOutVo;
import lombok.*;

import java.util.Date;

@Getter
@Builder
@ToString
public class JwtPayload {
    private Long idx;
    private String userId;
    private boolean isAdmin;
    private Long createDate;

    public static JwtPayload of(UserOutVo user){
        return JwtPayload.builder()
                .idx(user.getIdx())
                .userId(user.getId())
                .isAdmin(0 == user.getIdx())
                .createDate(new Date(System.currentTimeMillis()).getTime())
                .build();
    }

    public static JwtPayload of(User user){
        return JwtPayload.builder()
                .idx(user.getIdx())
                .userId(user.getId())
                .isAdmin(0 == user.getIdx())
                .createDate(new Date(System.currentTimeMillis()).getTime())
                .build();
    }
}
