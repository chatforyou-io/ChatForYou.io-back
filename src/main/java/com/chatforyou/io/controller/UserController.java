package com.chatforyou.io.controller;

import com.chatforyou.io.models.ValidateType;
import com.chatforyou.io.models.in.UserInVo;
import com.chatforyou.io.models.out.UserOutVo;
import com.chatforyou.io.services.AuthService;
import com.chatforyou.io.services.UserService;
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
    /**
     * 이메일 정규식
     * 비밀번호 최소 8자 {영문 특수문자} 포함
     */

    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(
            @RequestParam String id) {
        Map<String, Object> response = new HashMap<>();
        UserOutVo userOutVo = userService.findUserById(id);
        response.put("result", "success");
        response.put("userData", userOutVo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/check_nickname")
    public ResponseEntity<Map<String, Object>> checkNickName(@RequestParam String nickName) throws BadRequestException {
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        if (authService.validateStrByType(ValidateType.NICKNAME, nickName)) {
            throw new BadRequestException("already exist user NickName");
        }
        response.put("userData", false);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody UserInVo user) throws BadRequestException {
        Map<String, Object> response = new HashMap<>();
        UserOutVo userOutVo = userService.saveUser(user);
        response.put("result", "success");
        response.put("userData", userOutVo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PatchMapping("/update")
    public ResponseEntity<Map<String, Object>> updateUser(@RequestBody UserInVo user) throws BadRequestException {
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        response.put("userData", userService.updateUser(user));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteUser(@RequestBody UserInVo user) throws BadRequestException {
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
