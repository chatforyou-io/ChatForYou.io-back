package com.chatforyou.io.controller;

import com.chatforyou.io.models.ValidateType;
import com.chatforyou.io.models.in.UserVO;
import com.chatforyou.io.models.out.UserInfo;
import com.chatforyou.io.services.OpenViduService;
import com.chatforyou.io.services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
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
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestParam String id){
        Map<String, Object> response = new HashMap<>();
        UserInfo userInfo = userService.findUserById(id);
        response.put("result", "success");
        response.put("user_data", userInfo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 사용자 로그인 처리 메서드
     *
     * @param params 요청 본문에서 받은 사용자명과 비밀번호
     * @return 로그인 성공 여부에 따른 응답 (HTTP 상태 코드 포함)
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody(required = true) Map<String, String> params) {

        // 요청으로부터 사용자명과 비밀번호를 가져옴
        String id = params.get("id");
        String password = params.get("password");

        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        response.put("user_data", userService.getUserInfo(id, password));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/check_nick_name")
    public ResponseEntity<Map<String, Object>> checkNickName(@RequestParam String nickName){
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        response.put("isDuplicate", userService.validateStrByType(ValidateType.NICKNAME, nickName));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody UserVO user) throws BadRequestException {
        Map<String, Object> response = new HashMap<>();
        UserInfo userInfo = userService.saveUser(user);
        response.put("result", "success");
        response.put("user_data", userInfo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PatchMapping("/update")
    public ResponseEntity<Map<String, Object>> updateUser(@RequestBody UserVO user) throws BadRequestException {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("result", "success");
        response.put("updatedUser", userService.updateUser(user));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteUser(@RequestBody UserVO user) throws BadRequestException {
        Map<String, Object> response = new HashMap<String, Object>();
        boolean result = userService.deleteUser(user);
        if (result) {
            response.put("result", "success");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("result", "fail delete user");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
