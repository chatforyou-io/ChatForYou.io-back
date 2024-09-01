package com.chatforyou.io.controller;

import com.chatforyou.io.client.OpenViduHttpException;
import com.chatforyou.io.client.OpenViduJavaClientException;
import com.chatforyou.io.models.OpenViduWebhookData;
import com.chatforyou.io.services.ChatRoomService;
import com.chatforyou.io.services.OpenViduService;
import com.chatforyou.io.services.OpenViduWebhookService;
import com.chatforyou.io.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/openvidu")
@RequiredArgsConstructor
@Slf4j
public class OpenViduController {

    private final OpenViduService openViduService;
    private final OpenViduWebhookService openViduWebhookService;

    @PostMapping("/webhook")
    public void processOpenViduWebhook(
            @RequestBody String requestBody) throws OpenViduJavaClientException, OpenViduHttpException {

        openViduWebhookService.processWebhookEvent(JsonUtils.jsonToObj(requestBody, OpenViduWebhookData.class));
    }

    @GetMapping("/sessions")
    public ResponseEntity<Map<String, Object>> getSessions() throws OpenViduJavaClientException, OpenViduHttpException {
        Map<String, Object> response = new HashMap<>();
        response.put("chatRooms", openViduService.getActiveSessionOutVoList());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
