package com.chatforyou.io.controller;

import com.chatforyou.io.services.OpenViduService;
import com.chatforyou.io.services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody(required = true) Map<String, Object> params,
                                                          @CookieValue(name = OpenViduService.MODERATOR_TOKEN_NAME, defaultValue = "") String moderatorCookie,
                                                          @CookieValue(name = OpenViduService.PARTICIPANT_TOKEN_NAME, defaultValue = "") String participantCookie,
                                                          HttpServletResponse res){
        Map<String, Object> response = new HashMap<String, Object>();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteUser(@RequestBody(required = true) Map<String, Object> params,
                                                          @CookieValue(name = OpenViduService.MODERATOR_TOKEN_NAME, defaultValue = "") String moderatorCookie,
                                                          @CookieValue(name = OpenViduService.PARTICIPANT_TOKEN_NAME, defaultValue = "") String participantCookie,
                                                          HttpServletResponse res){
        Map<String, Object> response = new HashMap<String, Object>();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
