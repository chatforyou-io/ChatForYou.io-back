package com.chatforyou.io.entity;

import com.chatforyou.io.models.in.SocialUserInVo;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "SOCIAL_USER")
public class SocialUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDX", nullable = false)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_IDX", nullable = false)
    private User user;

    @Column(name = "PROVIDER", length = 50)
    private String provider;

    @Column(name = "ACCOUNT_ID", length = 50)
    private String accountId;

    @Column(name = "TOKEN_TYPE", length = 20)
    private String tokenType;

    @Column(name = "TYPE", length = 20)
    private String type;

    public static SocialUser ofUser(User user, SocialUserInVo socialUserInVo){
        return SocialUser.builder()
                .user(user)
                .provider(socialUserInVo.getProvider())
                .accountId(socialUserInVo.getProviderAccountId())
                .tokenType(socialUserInVo.getTokenType())
                .type(socialUserInVo.getType())
                .build();
    }

}