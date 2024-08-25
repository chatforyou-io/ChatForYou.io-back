package com.chatforyou.io.config;

import com.chatforyou.io.models.OpenViduData;
import com.chatforyou.io.models.out.ChatRoomOutVo;
import com.chatforyou.io.utils.RedisUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisConfigTest {

    Logger logger = LoggerFactory.getLogger(RedisConfigTest.class);
    ChatRoomOutVo chatRoomOutVo;

    @Autowired
    private RedisUtils redisUtils;

    RedisConfigTest(){
        chatRoomOutVo = ChatRoomOutVo.builder()
                .creator("sejon")
                .roomName("새로운 방")
                .sessionId(UUID.randomUUID().toString())
                .useRtc(false)
                .usePwd(false)
                .maxUserCount(5)
                .build();
    }

    @DisplayName("redis utils 테스트")
    @Test
    void redisUtilTest(){
        ChatRoomOutVo chatRoomOutVo = ChatRoomOutVo.builder()
                .creator("sejon")
                .roomName("새로운 방")
                .sessionId(UUID.randomUUID().toString())
                .useRtc(false)
                .usePwd(false)
                .maxUserCount(5)
                .build();

        redisUtils.setObject(chatRoomOutVo.getSessionId(), chatRoomOutVo);

        ChatRoomOutVo redisRoom = redisUtils.getObject(chatRoomOutVo.getSessionId(), ChatRoomOutVo.class);
        logger.info("room :::: {}", redisRoom.toString());

    }

    @DisplayName("redis utils ttl 테스트")
    @Test
    void redisUtilTtlTest(){

        redisUtils.setObject(chatRoomOutVo.getSessionId(), chatRoomOutVo, 3600, TimeUnit.SECONDS);

        ChatRoomOutVo redisRoom = redisUtils.getObject(chatRoomOutVo.getSessionId(), ChatRoomOutVo.class);
        logger.info("roomttl :::: {}", redisRoom.toString());
    }

    @DisplayName("redis utils expire 확인")
    @Test
    void redisUtilExpireTest(){
        redisUtils.setObject(chatRoomOutVo.getSessionId(), chatRoomOutVo, 3600, TimeUnit.SECONDS);
        try {
            Thread.sleep(10000); // 10초 대기 (10000ms)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long expired = redisUtils.getExpiredByTimeUnit(chatRoomOutVo.getSessionId(), TimeUnit.SECONDS);
        logger.info("expired ::::: {}", expired);

    }

    @DisplayName("redis chatroom 확인")
    @Test
    void redisChatRoomTest(){
        OpenViduData openViduData = redisUtils.getObject("a9ff5c3a-76f7-45a6-90f9-387b4d06d5a2", OpenViduData.class);
        logger.info("chatroom :::: {}", openViduData.toString());
        logger.info("session ::: {}", openViduData.getSession().toString());
        logger.info("connection ::: {}", openViduData.getSession().getConnections().toString());
        long expired = redisUtils.getExpiredByTimeUnit(chatRoomOutVo.getSessionId(), TimeUnit.SECONDS);
        logger.info("expired ::::: {}", expired);

    }

}
