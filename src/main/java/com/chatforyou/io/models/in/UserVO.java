package com.chatforyou.io.models.in;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {
    private Long idx;
    private String id;
    private String pwd;
    private String confirmPwd;
    private Boolean usePwd;
    private String nickName;
    private String name;
}
