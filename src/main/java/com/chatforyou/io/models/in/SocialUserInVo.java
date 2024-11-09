package com.chatforyou.io.models.in;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SocialUserInVo {
    private Long idx;
    private String provider;
    private String providerAccountId;
    private Boolean usePwd;
    private String nickName;
    private String name;
    private String tokenType;
    private String type;
}
