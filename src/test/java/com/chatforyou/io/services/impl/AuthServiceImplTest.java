package com.chatforyou.io.services.impl;

import com.chatforyou.io.entity.SocialUser;
import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.in.SocialUserInVo;
import com.chatforyou.io.models.out.UserOutVo;
import com.chatforyou.io.repository.SocialRepository;
import com.chatforyou.io.repository.UserRepository;
import com.chatforyou.io.services.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthServiceImplTest {
    Logger logger = LoggerFactory.getLogger(AuthServiceImplTest.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private SocialRepository socialRepository;

    @Autowired
    private UserRepository userRepository;


//    SnsUser {
//        provider: { provider: 'google', providerAccountId: string },
//        id: string,
//                name: 'Junho Lee (Juno)',
//                nickName: '110471538461028878767',
//                idx: 0
//    }

    @Test
    @DisplayName("소셜 유저 저장 테스트")
    public void saveSocialUser(){
        SocialUserInVo socialUser = SocialUserInVo.builder()
                .provider("google")
                .providerAccountId("social@test.com")
                .name("socialUser")
                .nickName("im_social_user")
                .build();

        User user = User.ofSocialUser(socialUser);
        userRepository.save(user);

        logger.info("user 저장 완료 :::: {}", user.toString());

        SocialUser socialUserEntity = socialRepository.save(SocialUser.ofUser(user, socialUser));
        logger.info("socialuser 저장 완료 ::: {}", socialUserEntity.toString());
    }

    @Test
    @DisplayName("소셜 유저 로그인 테스트 :: 로그인 이력 있는 경우")
    public void findSocialUser(){
        SocialUserInVo socialUser = SocialUserInVo.builder()
                .provider("google")
                .providerAccountId("social@test.com")
                .name("socialUser")
                .nickName("im_social_user")
                .build();

        UserOutVo loginUserInfo = authService.getSocialLoginUserInfo(socialUser);
        logger.info("로그인하는 유저 정보 :: {}", loginUserInfo.toString());
    }

    @Test
    @DisplayName("소셜 유저 로그인 테스트 :: 로그인 이력 없는 경우")
    public void findSocialUserNoLogined(){
        SocialUserInVo socialUser = SocialUserInVo.builder()
                .provider("google")
                .providerAccountId("social@test.com")
                .name("socialUser")
                .nickName("im_social_user")
                .build();

        UserOutVo loginUserInfo = authService.getSocialLoginUserInfo(socialUser);
        logger.info("로그인하는 유저 정보 :: {}", loginUserInfo.toString());
    }

}