package com.chatforyou.io.services.impl;

import com.chatforyou.io.config.SchedulerConfig;
import com.chatforyou.io.entity.SocialUser;
import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.AdminSessionData;
import com.chatforyou.io.models.in.SocialUserInVo;
import com.chatforyou.io.models.in.UserInVo;
import com.chatforyou.io.models.out.UserOutVo;
import com.chatforyou.io.repository.SocialRepository;
import com.chatforyou.io.repository.UserRepository;
import com.chatforyou.io.services.AuthService;
import com.chatforyou.io.services.SseService;
import com.chatforyou.io.services.UserService;
import com.chatforyou.io.utils.AuthUtils;
import com.chatforyou.io.utils.RedisUtils;
import com.chatforyou.io.utils.ThreadUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final SocialRepository socialRepository;
    private final RedisUtils redisUtils;
    private final AuthUtils authUtils;
    private final UserService userService;
    private final SseService sseService;
    private final SchedulerConfig schedulerConfig;

    private Map<String, AdminSessionData> adminSessions = new HashMap<>();

    @Override
    public boolean isAdminSessionValid(String sessionId) {
        if (sessionId.isBlank()) return false;

        AdminSessionData adminCookie = this.adminSessions.get(sessionId);
        if (adminCookie == null) return false;
        return adminCookie.getExpires() > new Date().getTime();
    }

    @Override
    public void putAdminSession(String sessionId, AdminSessionData data) {
        this.adminSessions.put(sessionId, data);
    }

    @Override
    public void delAdminSession(String sessionId) {
        this.adminSessions.remove(sessionId);
    }

    @Override
    public boolean checkEmailValidate(String token, String code) {
        return false;
    }

    @Override
    public UserOutVo getLoginUserInfo(String id, String pwd) {
        User user = userRepository.findByIdAndPwd(id, pwd)
                .orElseThrow(() -> new EntityNotFoundException("Can not find user"));

        if (!authUtils.getDecodeStr(user.getPwd().getBytes()).equals(authUtils.getDecodeStr(pwd.getBytes()))) {
            throw new EntityNotFoundException("Invalid User Id or Password");
        }

        // 마지막 로그인 시간 업데이트
        this.updateLastLoginTime(user);

        UserOutVo userOutVo = UserOutVo.of(user, false);

        this.userRedisJob(userOutVo);

        return userOutVo;
    }

    /**
     * 마지막 로그인 시간 업데이트
     *
     * @param user
     */
    private void updateLastLoginTime(User user) {
        user.setLastLoginDate(new Date().getTime());
        userRepository.save(user);
    }

    @Override
    public UserOutVo getSocialLoginUserInfo(SocialUserInVo socialUserInVo) throws BadRequestException {
        if (socialUserInVo.getProvider() == null || socialUserInVo.getProviderAccountId() == null) {
            throw new BadRequestException("Need Provider or providerAccountId");
        }

        User user = null;

        Optional<SocialUser> socialUser = socialRepository.findSocialUserByProviderAccountIdAndAndProvider(socialUserInVo.getProviderAccountId(), socialUserInVo.getProvider());
        if (socialUser.isPresent()) { // 소셜 로그인 유저 정보가 있다면
            user = socialUser.get().getUser();
            // 유저 로그인시간 update
            this.updateLastLoginTime(user);

            // userVo 로 변환
            UserOutVo userOutVo = UserOutVo.of(user, false);

            // 유저 레디스 저장
            this.userRedisJob(userOutVo);

            return userOutVo;
        } else { // 소셜 로그인 유저 정보가 없다면 user 에 insert
            user = User.ofSocialUser(socialUserInVo);
            userRepository.saveAndFlush(user);

            // social 에 insert
            SocialUser socialUserEntity = SocialUser.ofUser(user, socialUserInVo);
            socialRepository.saveAndFlush(socialUserEntity);

            UserOutVo userOutVo = UserOutVo.of(socialUserEntity, false);

            // 유저 레디스 저장
            this.userRedisJob(userOutVo);

            return userOutVo;
        }
    }

    /**
     * 유저 정보를 레디스에 저장
     *
     * @param user
     */
    private void userRedisJob(UserOutVo user) {
        ThreadUtils.executeAsyncTask(
                // 실행할 작업
                () -> {
                    try {
                        redisUtils.saveLoginUser(user);
                        // 유저 데이터 유효시간 업데이트
                        redisUtils.updateExpiredDate(user.getIdx());
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                },
                10, 10, "Save Login User Info",
                // 성공 시 후속 작업
                result -> {
                    if(Boolean.TRUE.equals(result)) {
                        sendSseEvent();
                    }
                }
        );
    }

    @Override
    public void logoutUser(UserInVo user) {
        if (userRepository.findUserByIdx(user.getIdx()).isEmpty()) {
            throw new EntityNotFoundException("Can not find user info");
        }

        ThreadUtils.executeAsyncTask(
                // 실행할 작업
                () -> {
                    try {
                        redisUtils.deleteLoginUser(user.getIdx());
                        return true;
                    } catch (Exception e) {
                        log.error("=== Unknown Exception :: {}", e.getMessage(), e);
                        return false;
                    }
                },
                10, 10, "Delete Login User Info",
                // 성공 시 후속 작업
                result -> {
                    if(Boolean.TRUE.equals(result)) {
                        sendSseEvent();
                    }
                }
        );
    }

    private void sendSseEvent() {
        try {
            List<UserOutVo> userList = userService.getUserList("", 0, 20);
            List<UserOutVo> loginUserList = userService.getLoginUserList("", 0, 20);
            sseService.notifyUserList(userList, loginUserList);
            log.info("Successfully End SSE Job");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
