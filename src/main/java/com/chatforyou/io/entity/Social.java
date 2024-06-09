package com.chatforyou.io.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "SOCIAL")
public class Social {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDX", nullable = false)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_IDX", nullable = false)
    private User userIdx;

    @Column(name = "ACCESS_TOKEN")
    private String accessToken;

    @Column(name = "EXPIRES_AT")
    private Long expiresAt;

    @Column(name = "PROVIDER", length = 50)
    private String provider;

    @Column(name = "PROVIDER_ACCOUNT_ID", length = 50)
    private String providerAccountId;

    @Column(name = "TOKEN_TYPE", length = 20)
    private String tokenType;

    @Column(name = "TYPE", length = 20)
    private String type;

}