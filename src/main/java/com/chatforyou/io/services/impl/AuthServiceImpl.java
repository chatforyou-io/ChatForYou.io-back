package com.chatforyou.io.services.impl;

import com.chatforyou.io.models.AdminSessionData;
import com.chatforyou.io.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
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
}
