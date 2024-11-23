package com.chatforyou.io.services.impl;

import com.chatforyou.io.entity.SocialUser;
import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.AdminSessionData;
import com.chatforyou.io.models.ValidateType;
import com.chatforyou.io.models.in.SocialUserInVo;
import com.chatforyou.io.models.in.UserInVo;
import com.chatforyou.io.models.out.UserOutVo;
import com.chatforyou.io.repository.SocialRepository;
import com.chatforyou.io.repository.UserRepository;
import com.chatforyou.io.services.AuthService;
import com.chatforyou.io.utils.AuthUtils;
import com.chatforyou.io.utils.RedisUtils;
import com.chatforyou.io.utils.ThreadUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final SocialRepository socialRepository;
	private final RedisUtils redisUtils;

	private Map<String, AdminSessionData> adminSessions = new HashMap<>();

	@Override
	public boolean isAdminSessionValid(String sessionId) {
		if(sessionId.isBlank()) return false;

		AdminSessionData adminCookie = this.adminSessions.get(sessionId);
		if(adminCookie == null) return false;
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

		if (!AuthUtils.getDecodeStr(user.getPwd().getBytes()).equals(AuthUtils.getDecodeStr(pwd.getBytes()))) {
			throw new EntityNotFoundException("Invalid User Id or Password");
		}

		UserOutVo userOutVo = UserOutVo.of(user, false);

		userRedisJob(userOutVo);

		return userOutVo;
	}

	@Override
	public UserOutVo getSocialLoginUserInfo(SocialUserInVo socialUserInVo) throws BadRequestException {
		if (socialUserInVo.getProvider() == null || socialUserInVo.getProviderAccountId() == null) {
			throw new BadRequestException("Need Provider or providerAccountId");
		}
		Optional<SocialUser> socialUser = socialRepository.findSocialUserByProviderAccountIdAndAndProvider(socialUserInVo.getProviderAccountId(), socialUserInVo.getProvider());
		User user = null;
		if (socialUser.isPresent()) { // 소셜 로그인 유저 정보가 있다면
			UserOutVo userOutVo = UserOutVo.of(socialUser.get(), false);

			// 유저 레디스 저장
			userRedisJob(userOutVo);

			return userOutVo;
		} else { // 소셜 로그인 유저 정보가 없다면
			// user 에 insert
			user = User.ofSocialUser(socialUserInVo);
			userRepository.saveAndFlush(user);

			// social 에 insert
			SocialUser socialUserEntity = SocialUser.ofUser(user, socialUserInVo);
			socialRepository.saveAndFlush(socialUserEntity);

			UserOutVo userOutVo = UserOutVo.of(socialUserEntity, false);

			// 유저 레디스 저장
			userRedisJob(userOutVo);

			return userOutVo;
		}
	}

	@Override
	public void logoutUser(UserInVo user) {
		if (userRepository.findUserByIdx(user.getIdx()).isEmpty()) {
			throw new EntityNotFoundException("Can not find user info");
		}

		ThreadUtils.runTask(()->{
			try {
				redisUtils.deleteLoginUser(user.getIdx());
				return true;
			} catch (Exception e) {
				log.error("=== Error User :: {}", user.toString());
				log.error("=== Unknown Exception :: {} : {}", e.getMessage(), e);
				return false;
			}
		}, 10, 10, "Delete Login User Info");
	}

	@Override
	public boolean validateStrByType(ValidateType type, String str) {
		switch (type) {
			case ID:
				return userRepository.checkExistsById(str);
			case NICKNAME:
				return userRepository.checkExistsByNickName(str);
			case PASSWORD:
				// TODO passwd validate 체크 필요
				break;
//			case CHATROOM_NAME:
//				return chatRoomRepository.checkExistsByRoomName(str);
		}

		return false;
	}

	/**
	 * 유저 정보를 레디스에 저장
	 * @param user
	 */
	private void userRedisJob(UserOutVo user){
		ThreadUtils.runTask(()->{
			try {
				redisUtils.saveLoginUser(user);
				return true;
			} catch (Exception e) {
				return false;
			}
		}, 10, 10, "Save Login User Info");
	}

}
