package com.chatforyou.io.services.impl;

import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.AdminSessionData;
import com.chatforyou.io.models.ValidateType;
import com.chatforyou.io.models.out.UserOutVo;
import com.chatforyou.io.repository.ChatRoomRepository;
import com.chatforyou.io.repository.UserRepository;
import com.chatforyou.io.services.AuthService;
import com.chatforyou.io.utils.AuthUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final ChatRoomRepository chatRoomRepository;

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

		return UserOutVo.of(user);
	}

	@Override
	public boolean validateStrByType(ValidateType type, String str) throws BadRequestException {
		switch (type) {
			case ID:
				return userRepository.checkExistsById(str);
			case NICKNAME:
				return userRepository.checkExistsByNickName(str);
			case PASSWORD:
				// TODO passwd validate 체크 필요
				break;
			case CHATROOM_NAME:
				return chatRoomRepository.checkExistsByRoomName(str);
		}

		return false;
	}
}
