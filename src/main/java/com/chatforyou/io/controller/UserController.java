package com.chatforyou.io.controller;

import com.chatforyou.io.models.in.UserVO;
import com.chatforyou.io.models.out.UserInfo;
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

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestParam String id,
//                                                          @CookieValue(name = OpenViduService.MODERATOR_TOKEN_NAME, defaultValue = "") String moderatorCookie,
//                                                          @CookieValue(name = OpenViduService.PARTICIPANT_TOKEN_NAME, defaultValue = "") String participantCookie,
                                                          HttpServletResponse res){
        Map<String, Object> response = new HashMap<>();
        UserInfo userInfo = userService.getUserById(id);
        response.put("result", "success");
        response.put("saved_user", userInfo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/check_nick_name")
    public ResponseEntity<Map<String, Object>> checkNickName(@RequestParam String nickName,
//                                                          @CookieValue(name = OpenViduService.MODERATOR_TOKEN_NAME, defaultValue = "") String moderatorCookie,
//                                                          @CookieValue(name = OpenViduService.PARTICIPANT_TOKEN_NAME, defaultValue = "") String participantCookie,
                                                          HttpServletResponse res){
        Map<String, Object> response = new HashMap<>();

        response.put("result", "success");
        response.put("isDuplicate", userService.checkNickName(nickName));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createUser(@ModelAttribute UserVO user,
//                                                          @CookieValue(name = OpenViduService.MODERATOR_TOKEN_NAME, defaultValue = "") String moderatorCookie,
//                                                          @CookieValue(name = OpenViduService.PARTICIPANT_TOKEN_NAME, defaultValue = "") String participantCookie,
                                                          HttpServletResponse res){
        Map<String, Object> response = new HashMap<String, Object>();
        UserInfo userInfo = userService.saveUser(user);
        response.put("result", "success");
        response.put("saved_user", userInfo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PatchMapping("/update")
    public ResponseEntity<Map<String, Object>> updateUser(@ModelAttribute UserVO user,
//                                                          @CookieValue(name = OpenViduService.MODERATOR_TOKEN_NAME, defaultValue = "") String moderatorCookie,
//                                                          @CookieValue(name = OpenViduService.PARTICIPANT_TOKEN_NAME, defaultValue = "") String participantCookie,
                                                          HttpServletResponse res){
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("result", "success");
        response.put("updatedUser", userService.updateUser(user));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteUser(@ModelAttribute UserVO user,
//                                                          @CookieValue(name = OpenViduService.MODERATOR_TOKEN_NAME, defaultValue = "") String moderatorCookie,
//                                                          @CookieValue(name = OpenViduService.PARTICIPANT_TOKEN_NAME, defaultValue = "") String participantCookie,
                                                          HttpServletResponse res){
        Map<String, Object> response = new HashMap<String, Object>();
        boolean result = userService.deleteUser(user);
        if (result) {
            response.put("result", "success");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("result", "delete failed");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

}
