package com.chatforyou.io.controller;

import com.chatforyou.io.client.OpenViduHttpException;
import com.chatforyou.io.client.OpenViduJavaClientException;
import com.chatforyou.io.models.JwtPayload;
import com.chatforyou.io.models.in.ChatRoomInVo;
import com.chatforyou.io.models.out.ChatRoomOutVo;
import com.chatforyou.io.services.ChatRoomService;
import com.chatforyou.io.services.JwtService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/chatroom")
public class ChatRoomController {
    private final JwtService jwtService;
    private final ChatRoomService chatRoomService;

    /**
     * 새로운 채팅방을 생성
     *
     * @param chatRoomInVo 채팅방 생성에 필요한 정보를 담고 있는 객체
     * @return 생성된 채팅방 정보를 포함한 ResponseEntity
     * @throws BadRequestException 잘못된 요청일 경우 발생하는 예외
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createChatRoom(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody(required = true) ChatRoomInVo chatRoomInVo) throws BadRequestException {
        JwtPayload payload = jwtService.verifyAccessToken(bearerToken);
        ChatRoomOutVo chatRoom = chatRoomService.createChatRoom(chatRoomInVo, payload);
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        response.put("roomData", chatRoom);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 특정 세션 ID를 기반으로 채팅방 정보를 조회
     *
     * @param sessionId 조회할 채팅방의 세션 ID
     * @return 채팅방 정보를 포함한 ResponseEntity
     * @throws BadRequestException 잘못된 요청일 경우 발생하는 예외
     */
    @GetMapping("/info/{sessionId}")
    public ResponseEntity<Map<String, Object>> getChatRoomInfo(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("sessionId") String sessionId) throws BadRequestException {
        jwtService.verifyAccessToken(bearerToken);
        Map<String, Object> response = new HashMap<>();
        ChatRoomOutVo chatRoom = chatRoomService.findChatRoomBySessionId(sessionId);
        response.put("result", "success");
        response.put("roomData", chatRoom);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 특정 세션 ID를 기반으로 채팅방 정보를 수정
     *
     * @param sessionId 수정할 채팅방의 세션 ID
     * @param chatRoomInVo 수정할 정보를 담고 있는 객체
     * @return 수정된 채팅방 정보를 포함한 ResponseEntity
     * @throws BadRequestException 잘못된 요청일 경우 발생하는 예외
     */
    @PatchMapping("/update/{sessionId}")
    public ResponseEntity<Map<String, Object>> updateChatRoom(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("sessionId") String sessionId,
            @RequestBody ChatRoomInVo chatRoomInVo) throws BadRequestException {
        JwtPayload payload = jwtService.verifyAccessToken(bearerToken);
        Map<String, Object> response = new HashMap<>();
        ChatRoomOutVo chatRoom = chatRoomService.updateChatRoom(sessionId, chatRoomInVo, payload);
        response.put("result", "success");
        response.put("roomData", chatRoom);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 특정 세션 ID를 기반으로 채팅 방 삭제
     * TODO creator 정보와 비교 필요
     * @param sessionId 삭제할 채팅방의 세션 ID
     * @return 삭제 성공 여부를 포함한 ResponseEntity
     * @throws BadRequestException 잘못된 요청일 경우 발생하는 예외
     * @throws OpenViduJavaClientException OpenVidu 클라이언트 관련 예외
     * @throws OpenViduHttpException OpenVidu HTTP 요청 관련 예외
     */
    @DeleteMapping("/delete/{sessionId}")
    public ResponseEntity<Map<String, Object>> deleteChatRoom(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("sessionId") String sessionId) throws OpenViduJavaClientException, OpenViduHttpException, BadRequestException {
        JwtPayload payload = jwtService.verifyAccessToken(bearerToken);
        Map<String, Object> response = new HashMap<>();
        response.put("result", chatRoomService.deleteChatRoom(sessionId, payload, false) ? "success" : "Fail Delete ChatRoom");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 모든 채팅방 목록을 조회
     *
     * @param keyword 검색 키워드 (선택 사항)
     * @param pageNumStr 페이지 번호 (기본값 0)
     * @param pageSizeStr 페이지 크기 (기본값 9)
     * @return 채팅방 목록을 포함한 ResponseEntity
     * @throws BadRequestException 잘못된 요청일 경우 발생하는 예외
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getChatRoomList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNum", required = false, defaultValue = "0") String pageNumStr,
            @RequestParam(value = "pageSize", required = false, defaultValue = "9") String pageSizeStr
    ) throws BadRequestException {
        Map<String, Object> response = new LinkedHashMap<>();
        List<ChatRoomOutVo> chatRoomList = chatRoomService.getChatRoomList(keyword, Integer.parseInt(pageNumStr), Integer.parseInt(pageSizeStr));
        response.put("result", "success");
        response.put("count", chatRoomList.size());
        response.put("roomList", chatRoomList);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * OpenVidu 세션 데이터를 조회
     *
     * @param sessionId 조회할 세션 ID
     * @return OpenVidu 세션 데이터를 포함한 ResponseEntity
     * @throws BadRequestException 잘못된 요청일 경우 발생하는 예외
     */
    @GetMapping("/openvidu_data")
    public ResponseEntity<Map<String, Object>> getOpenViduData(
            @RequestHeader("Authorization") String bearerToken,
            @RequestParam String sessionId) throws BadRequestException {
        jwtService.verifyAccessToken(bearerToken);
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        response.put("openViduData", chatRoomService.getOpenviduDataBySessionId(sessionId));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 특정 세션 ID와 사용자 인덱스를 기반으로 연결 정보를 조회
     *
     * @param sessionId 조회할 세션 ID
     * @param userIdx 조회할 사용자 인덱스
     * @return 연결 정보를 포함한 ResponseEntity
     * @throws BadRequestException 잘못된 요청일 경우 발생하는 예외
     */
    @GetMapping("/connection_token/{sessionId}")
    public ResponseEntity<Map<String, Object>> getConnectionInfo(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("sessionId") String sessionId,
            @RequestParam("user_idx") String userIdx) throws BadRequestException {
        jwtService.verifyAccessToken(bearerToken);
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        response.put("connectionTokenData", chatRoomService.getConnectionInfo(sessionId, Long.parseLong(userIdx)));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 사용자를 특정 채팅방에 입장시킵니다.
     *
     * @param sessionId 입장할 채팅방의 세션 ID
     * @param userIdx 입장할 사용자 인덱스
     * @return 입장 결과를 포함한 ResponseEntity
     * @throws BadRequestException 잘못된 요청일 경우 발생하는 예외
     * @throws OpenViduJavaClientException OpenVidu 클라이언트 관련 예외
     * @throws OpenViduHttpException OpenVidu HTTP 요청 관련 예외
     */
    @GetMapping("/join/{sessionId}")
    public ResponseEntity<Map<String, Object>> joinChatRoom(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("sessionId") String sessionId,
            @RequestParam("user_idx") String userIdx) throws BadRequestException, OpenViduJavaClientException, OpenViduHttpException {
        JwtPayload payload = jwtService.verifyAccessToken(bearerToken);
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        response.put("joinData", chatRoomService.joinChatRoom(sessionId, Long.parseLong(userIdx), payload));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 채팅방 비밀번호를 확인
     *
     * @param sessionId 확인할 채팅방의 세션 ID
     * @param chatRoomInVo 비밀번호 정보를 담고 있는 객체
     * @return 비밀번호 확인 결과를 포함한 ResponseEntity
     * @throws BadRequestException 잘못된 요청일 경우 발생하는 예외
     */
    @PostMapping("/check_password/{sessionId}")
    public ResponseEntity<Map<String, Object>> checkRoomPassword(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("sessionId") String sessionId,
            @RequestBody ChatRoomInVo chatRoomInVo) throws BadRequestException {
        jwtService.verifyAccessToken(bearerToken);
        Map<String, Object> response = new HashMap<>();
        response.put("result", chatRoomService.checkRoomPassword(sessionId, chatRoomInVo.getPwd()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
