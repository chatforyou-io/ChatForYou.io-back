package com.chatforyou.io.utils;

import com.chatforyou.io.config.RedisConfig;
import com.chatforyou.io.models.in.ChatRoomInVo;
import com.chatforyou.io.models.out.UserOutVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.dengliming.redismodule.redisearch.RediSearch;
import io.github.dengliming.redismodule.redisearch.client.RediSearchClient;
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

import java.util.List;


@SpringBootTest
@Transactional
public class RedisSearchTest {
    Logger logger = LoggerFactory.getLogger(RedisSearchTest.class);
    @Autowired
    RedisConfig redisConfig;

    @Autowired
    RediSearchClient rediSearchClient;

    @Test
    @DisplayName("레디스 전체 데이터 검색")
    void searchAllRedisData() throws BadRequestException {
        // RediSearch 클라이언트를 사용하여 chatRoomIndex 검색 실행
        RediSearch chatRoomSearch = rediSearchClient.getRediSearch("chatRoomIndex");

        // 모든 문서를 검색
        List<Document> documents = chatRoomSearch.search("*", new SearchOptions()).getDocuments();

        // 모든 검색 결과 순회
        for (Document doc : documents) {
            logger.info("sessionId : {}",doc.getId());
            logger.info("roomName : {}",doc.getFields().get("roomName"));
        }
    }

    @Test
    @DisplayName("레디스 단일 조건 검색")
    void searchRedis() throws BadRequestException {
        // 검색 실행
        RediSearch chatRoomSearch = rediSearchClient.getRediSearch("chatRoomIndex");
        // Redis 검색 결과에서 openvidu 필드의 JSON 문자열 가져오기
        List<Document> documents = chatRoomSearch.search(
                "@creator:*teri*",
                new SearchOptions()
        ).getDocuments();

        // 모든 검색 결과 순회
        for (Document doc : documents) {
            logger.info("sessionId : {}",doc.getId());
            logger.info("roomName : {}",doc.getFields().get("roomName"));
            logger.info("creator : {}",doc.getFields().get("creator"));
        }
    }

    @Test
    @DisplayName("레디스 다중 조건 검색")
    void searchKeyword() throws BadRequestException {
        // 검색 실행
        RediSearch chatRoomSearch = rediSearchClient.getRediSearch("chatRoomIndex");
        // Redis 검색 결과에서 openvidu 필드의 JSON 문자열 가져오기
        int pageNumber = 0;  // 원하는 페이지 번호
        int pageSize = 5;    // 한 페이지에 표시할 항목 수
        String keyword = "aabb";
        String queryParam = "((@creator:*" + keyword + "*) | (@roomName:*" + keyword + "*))";


        List<Document> documents = chatRoomSearch.search(
                queryParam,
                new SearchOptions().page(pageNumber * pageSize, pageSize)
        ).getDocuments();


        // 모든 검색 결과 순회
        for (Document doc : documents) {
            logger.info("sessionId : {}",doc.getId());
            logger.info("roomName : {}",doc.getFields().get("roomName"));
        }
    }

    @Test
    @DisplayName("레디스 페이징 검색")
    void searchAllPaging() throws BadRequestException {
        // 검색 실행
        RediSearch chatRoomSearch = rediSearchClient.getRediSearch("chatRoomIndex");
        // Redis 검색 결과에서 openvidu 필드의 JSON 문자열 가져오기
        int pageNumber = 0;  // 원하는 페이지 번호
        int pageSize = 5;    // 한 페이지에 표시할 항목 수

        List<Document> documents = chatRoomSearch.search(
                "*",
                new SearchOptions().page(pageNumber * pageSize, pageSize)
        ).getDocuments();


        logger.info("result :: {}", documents);
    }

    @Test
    @DisplayName("레디스 특수문자 검색")
    void searchSpecialChar() throws BadRequestException {
        // 검색 실행
        RediSearch chatRoomSearch = rediSearchClient.getRediSearch("chatRoomIndex");
        // Redis 검색 결과에서 openvidu 필드의 JSON 문자열 가져오기
        int pageNumber = 0;  // 원하는 페이지 번호
        int pageSize = 5;    // 한 페이지에 표시할 항목 수
        String keyword = "!zx";
        String queryParam = "@roomName:*" + escapeSpecialCharacters(keyword) + "*";

        List<Document> documents = chatRoomSearch.search(
                queryParam,
                new SearchOptions().page(pageNumber * pageSize, pageSize)
        ).getDocuments();

        logger.info("queryParam :: {}", queryParam);
        logger.info("result :: {}", documents);
    }

    private String escapeSpecialCharacters(String keyword) {
        // 특수문자를 정의 (이스케이프해야 할 문자들)
        String specialCharacters = "[!@#?$%^&*()\\-+=<>|\\[\\]{}.,]";

        // 정규 표현식을 사용하여 특수문자 앞에 '\'를 추가
        return keyword.replaceAll(specialCharacters, "\\\\$0");
    }


    @Test
    @DisplayName("레디스 로그인 유저 검색")
    void searchLoginUser() throws BadRequestException {
        // 검색 실행
        RediSearch chatRoomSearch = rediSearchClient.getRediSearch("userIndex");
        // Redis 검색 결과에서 openvidu 필드의 JSON 문자열 가져오기
        int pageNumber = 0;  // 원하는 페이지 번호
        int pageSize = 5;    // 한 페이지에 표시할 항목 수
        String keyword = "id";
        String queryParam = "((@userId:*" + keyword + "*) | (@nickName:*" + keyword + "*))";

        List<Document> documents = chatRoomSearch.search(
                queryParam,
                new SearchOptions().page(pageNumber * pageSize, pageSize)
        ).getDocuments();

        logger.info("queryParam :: {}", queryParam);
        logger.info("result :: {}", documents);
    }
}