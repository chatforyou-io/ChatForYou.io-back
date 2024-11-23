package com.chatforyou.io.models.in;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateVo {
    @NonNull
    private Long idx;
    private String id;
    private String newPwd;
    private String nickName;
}
