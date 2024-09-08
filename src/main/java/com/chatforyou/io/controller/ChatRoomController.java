package com.chatforyou.io.controller;

import com.chatforyou.io.client.OpenViduHttpException;
import com.chatforyou.io.client.OpenViduJavaClientException;
import com.chatforyou.io.models.in.ChatRoomInVo;
import com.chatforyou.io.models.out.ChatRoomOutVo;
import com.chatforyou.io.services.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/chatroom")
public class ChatRoomController {

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
            @RequestBody(required = true) ChatRoomInVo chatRoomInVo) throws BadRequestException {
        Map<String, Object> response = new HashMap<>();

        ChatRoomOutVo chatRoom = chatRoomService.createChatRoom(chatRoomInVo);
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
            @PathVariable("sessionId") String sessionId) throws BadRequestException {
        Map<String, Object> response = new HashMap<>();

        ChatRoomOutVo chatRoom = chatRoomService.findChatRoomBySessionId(sessionId);
        response.put("result", "success");
        response.put("roomData", chatRoom);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 특정 세션 ID를 기반으로 채팅방 정보를 수정
     *
     * @param sessionId 조회할 채팅방의 세션 ID
     * @return 채팅방 정보를 포함한 ResponseEntity
     * @throws BadRequestException 잘못된 요청일 경우 발생하는 예외
     */
    @PatchMapping("/update/{sessionId}")
    public ResponseEntity<Map<String, Object>> updateChatRoom(
            @PathVariable("sessionId") String sessionId,
            @RequestBody ChatRoomInVo chatRoomInVo) throws BadRequestException {
        Map<String, Object> response = new HashMap<>();

        ChatRoomOutVo chatRoom = chatRoomService.updateChatRoom(sessionId, chatRoomInVo);
        response.put("result", "success");
        response.put("roomData", chatRoom);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 특정 세션 ID를 기반으로 채팅 방 삭제
     * TODO creator 정보와 비교 필요
     * @param sessionId 삭제할 채팅방의 세션 ID
     * @return 삭제 성공 실패 여부
     * @throws BadRequestException 잘못된 요청일 경우 발생하는 예외
     */
    @DeleteMapping("/delete/{sessionId}")
    public ResponseEntity<Map<String, Object>> deleteChatRoom(
            @PathVariable("sessionId") String sessionId) throws OpenViduJavaClientException, OpenViduHttpException {
        Map<String, Object> response = new HashMap<>();
        response.put("result", chatRoomService.deleteChatRoom(sessionId) ? "success" : "Fail Delete ChatRoom");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 모든 채팅방 목록을 조회
     *
     * @return 채팅방 목록을 포함한 ResponseEntity
     * @throws BadRequestException 잘못된 요청일 경우 발생하는 예외
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getChatRoomList() {
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        response.put("roomList", chatRoomService.getChatRoomList());
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
            @RequestParam String sessionId) throws BadRequestException {
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
            @PathVariable("sessionId") String sessionId,
            @RequestParam("user_idx") String userIdx) throws BadRequestException {
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
     */
    @GetMapping("/join/{sessionId}")
    public ResponseEntity<Map<String, Object>> joinChatRoom(
            @PathVariable("sessionId") String sessionId,
            @RequestParam("user_idx") String userIdx) throws BadRequestException, OpenViduJavaClientException, OpenViduHttpException {
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        response.put("joinData", chatRoomService.joinChatRoom(sessionId, Long.parseLong(userIdx)));
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
            @PathVariable("sessionId") String sessionId,
            @RequestBody ChatRoomInVo chatRoomInVo) throws BadRequestException {
        Boolean result = chatRoomService.checkRoomPassword(sessionId, chatRoomInVo.getPwd());
        if (Boolean.FALSE.equals(result)) {
            throw new RuntimeException("Unknown Server Exception");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
