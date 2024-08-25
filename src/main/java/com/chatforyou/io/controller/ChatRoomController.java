package com.chatforyou.io.controller;

import com.chatforyou.io.models.in.ChatRoomInVo;
import com.chatforyou.io.models.out.ChatRoomOutVo;
import com.chatforyou.io.services.ChatRoomService;
import com.chatforyou.io.services.OpenViduService;
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

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createChatRoom(
            @RequestBody(required = true) ChatRoomInVo chatRoomInVo) throws BadRequestException {
        Map<String, Object> response = new HashMap<>();

        ChatRoomOutVo chatRoom = chatRoomService.createChatRoom(chatRoomInVo);
        response.put("result", chatRoom);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 채팅방 조회
    // 유저 인덱스 추가
    @GetMapping("/{sessionId}/info")
    public ResponseEntity<Map<String, Object>> getChatRoomInfo(
            @PathVariable("sessionId") String sessionId) throws BadRequestException {
        Map<String, Object> response = new HashMap<>();

        ChatRoomOutVo chatRoom = chatRoomService.getChatRoomBySessionId(sessionId);
        response.put("result", chatRoom);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/list")
    // 유저 인덱스 추가
    public ResponseEntity<Map<String, Object>> getChatRoomList() throws BadRequestException {
        Map<String, Object> response = new HashMap<>();
        response.put("result", chatRoomService.getChatRoomList());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/openvidu_data")
    public ResponseEntity<Map<String, Object>> getOpenViduData(
            @RequestParam String sessionId) throws BadRequestException {
        Map<String, Object> response = new HashMap<>();
        response.put("result", chatRoomService.getOpenviduDataBySessionId(sessionId));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{sessionId}/connection_token")
    public ResponseEntity<Map<String, Object>> getConnectionInfo(
            @PathVariable("sessionId") String sessionId,
            @RequestParam("user_idx") String userIdx) throws BadRequestException {
        Map<String, Object> response = new HashMap<>();
        response.put("result", chatRoomService.getConnectionInfo(sessionId, Long.parseLong(userIdx)));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 방 유저 입장
    @PostMapping("/{sessionId}/join")
    public ResponseEntity<Map<String, Object>> joinChatRoom(
            @PathVariable("sessionId") String sessionId,
            @RequestParam("user_idx")String userIdx) throws BadRequestException {
        Map<String, Object> response = new HashMap<>();
        response.put("result", chatRoomService.joinChatRoom(sessionId, Long.parseLong(userIdx)));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 방 비밀번호 확인
    @PostMapping("/{sessionId}/check_password")
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
