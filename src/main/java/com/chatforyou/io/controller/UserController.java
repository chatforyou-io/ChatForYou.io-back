package com.chatforyou.io.controller;

import com.chatforyou.io.models.JwtPayload;
import com.chatforyou.io.models.ValidateType;
import com.chatforyou.io.models.in.UserInVo;
import com.chatforyou.io.models.in.UserUpdateVo;
import com.chatforyou.io.models.out.UserOutVo;
import com.chatforyou.io.services.JwtService;
import com.chatforyou.io.services.UserService;
import com.chatforyou.io.utils.AuthUtils;
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
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    /**
     * TODO 개발 필요
     * 이메일 정규식
     * 비밀번호 최소 8자 {영문 특수문자} 포함
     */

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthUtils authUtils;

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(
            @RequestParam String id) {
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        response.put("userData", userService.findUserById(id));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/check_nickname")
    public ResponseEntity<Map<String, Object>> checkNickName(
            @RequestParam String nickName) throws BadRequestException {
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        if (authUtils.validateStrByType(ValidateType.NICKNAME, nickName)) {
            throw new BadRequestException("already exist user NickName");
        }
        response.put("userData", false);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody UserInVo user) throws BadRequestException {
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        response.put("userData", userService.saveUser(user));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PatchMapping("/update")
    public ResponseEntity<Map<String, Object>> updateUser(@RequestHeader("Authorization") String bearerToken,
                                                          @RequestBody UserUpdateVo userVo) throws BadRequestException {
        JwtPayload jwtPayload = jwtService.validateAccessToken(bearerToken);
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        response.put("userData", userService.updateUser(userVo, jwtPayload));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/update/pwd")
    public ResponseEntity<Map<String, Object>> updateUserPasswd(@RequestHeader("Authorization") String bearerToken,
                                                                @RequestBody UserUpdateVo userVo) throws BadRequestException {
        JwtPayload jwtPayload = jwtService.validateAccessToken(bearerToken);
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        response.put("userData", userService.updateUserPwd(userVo, jwtPayload));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteUser(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody UserInVo user) throws BadRequestException {
        JwtPayload jwtPayload = jwtService.validateAccessToken(bearerToken);
        userService.deleteUser(user, jwtPayload);
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 현재 로그인된 유저 목록 조회
     *
     * @param keyword 검색 키워드 (선택 사항)
     * @param pageNumStr 페이지 번호 (기본값 0)
     * @param pageSizeStr 페이지 크기 (기본값 20)
     * @return 로그인 유저 목록을 포함한 ResponseEntity
     * @throws BadRequestException 잘못된 요청일 경우 발생하는 예외
     */
    @GetMapping("/list/current")
    public ResponseEntity<Map<String, Object>> getLoginUserList(
            @RequestHeader("Authorization") String bearerToken,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNum", required = false, defaultValue = "0") String pageNumStr,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") String pageSizeStr
    ) throws BadRequestException {
        return buildUserListResponse(bearerToken, keyword, pageNumStr, pageSizeStr,
                userService::getLoginUserList);
    }

    /**
     * 전체 유저 목록 조회
     *
     * @param keyword 검색 키워드 (선택 사항)
     * @param pageNumStr 페이지 번호 (기본값 0)
     * @param pageSizeStr 페이지 크기 (기본값 20)
     * @return 로그인 유저 목록을 포함한 ResponseEntity
     * @throws BadRequestException 잘못된 요청일 경우 발생하는 예외
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getUserList(
            @RequestHeader("Authorization") String bearerToken,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNum", required = false, defaultValue = "0") String pageNumStr,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") String pageSizeStr
    ) throws BadRequestException {
        return buildUserListResponse(bearerToken, keyword, pageNumStr, pageSizeStr,
                userService::getUserList);
    }

    /**
     * 사용자 리스트를 조회하기 위한 함수형 인터페이스.
     * keyword, pageNum, pageSize를 입력으로 받고, List<UserOutVo>를 반환
     */
    @FunctionalInterface
    private interface UserListFunction {
        List<UserOutVo> execute(String keyword, int pageNum, int pageSize);
    }

    /**
     * UserList 조회에서 공통된 부분 추출
     * @param bearerToken 유저 토큰
     * @param keyword 검색 키워드
     * @param pageNumStr 페이지 번호
     * @param pageSizeStr 페이지 사이즈
     * @param userListFunction 유저 리스트 조회 함수
     * @return 조건에 따라 조회된 userList
     * @throws BadRequestException 잘못된 요청일 경우 발생하는 예외
     */
    private ResponseEntity<Map<String, Object>> buildUserListResponse(
            String bearerToken,
            String keyword,
            String pageNumStr,
            String pageSizeStr,
            UserListFunction userListFunction
    ) throws BadRequestException {
        jwtService.validateAccessToken(bearerToken);
        int pageNum = Integer.parseInt(pageNumStr);
        int pageSize = Integer.parseInt(pageSizeStr);

        List<UserOutVo> userList = userListFunction.execute(keyword, pageNum, pageSize);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", "success");
        response.put("totalCount", userList.size());
        response.put("pageNum", pageNum == 0 ? 1 : pageNum);
        response.put("pageSize", pageSize);
        response.put("userList", userList);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
