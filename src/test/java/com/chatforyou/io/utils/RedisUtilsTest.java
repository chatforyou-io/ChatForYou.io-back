package com.chatforyou.io.utils;

import com.chatforyou.io.models.DataType;
import com.chatforyou.io.models.SearchType;
import com.chatforyou.io.models.out.UserOutVo;
import io.github.dengliming.redismodule.redisearch.RediSearch;
import io.github.dengliming.redismodule.redisearch.index.Document;
import io.github.dengliming.redismodule.redisearch.search.SearchOptions;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@SpringBootTest
@Transactional
class RedisUtilsTest {
    Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    @Autowired
    private RedisUtils redisUtils;

    @Test
    @DisplayName("레디스 로그인 저장")
    void saveLoginUser() throws BadRequestException {
        UserOutVo user = UserOutVo.builder()
                .idx(2L)
                .id("adsfasdfd")
                .name("테스트이123123름")
                .nickName("test name")
                .pwd("test pwd")
                .build();

        redisUtils.saveLoginUser(user);
    }

    @Test
    @DisplayName("레디스 저장된 모든 유저 검색")
    void searchLoginUser() throws BadRequestException {

        Set<String> strSet = redisUtils.getKeysByPattern("user");

        for(String key : strSet){
            UserOutVo user = redisUtils.getRedisDataByDataType(key, DataType.LOGIN_USER, UserOutVo.class);
            logger.info("user :: {}", user.toString());
        }
    }

    @Test
    @DisplayName("레디스 특정 유저 검색")
    void searchLoginUserByKeyword() throws BadRequestException {
        List<Document> documents = redisUtils.searchByKeyword(SearchType.LOGIN_USER, "id", 0, 20);

        logger.info("result :: {}", documents);
    }
}