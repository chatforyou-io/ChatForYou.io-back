package com.chatforyou.io.services.impl;

import com.chatforyou.io.entity.User;
import com.chatforyou.io.repository.UserRepository;
import com.chatforyou.io.utils.AuthUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserRepository repository;

    @Test
    @DisplayName("유저 저장 테스트")
    void saveUser(){
        User user = User.builder()
                .id("test")
                .pwd("test")
                .usePwd(true)
                .createDate(new Date().getTime())
                .name("test")
                .nickName("testnick")
                .build();
        repository.save(user);

    }

    @Test
    @DisplayName("base64 인코딩 디코딩")
    void baseEncodeAndDecode(){
        final String origin = "testPassword";
        String encodeStr = AuthUtils.getEncodeStr(origin);
        System.out.println("encodePwd :: "+ encodeStr);
        String decodePwd = AuthUtils.getDecodeStr(encodeStr.getBytes());
        System.out.println("decodePwd :: "+ decodePwd);
        assertEquals(origin, decodePwd);
    }
}