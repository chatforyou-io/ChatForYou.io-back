package com.chatforyou.io.services;

import com.chatforyou.io.models.AdminSessionData;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service

public class AuthService {
	public static final String ADMIN_COOKIE_NAME = "ovCallAdminToken";
	public Map<String, AdminSessionData> adminSessions = new HashMap<String, AdminSessionData>();


	public boolean isAdminSessionValid(String sessionId) {
		if(sessionId.isBlank()) return false;
		
		AdminSessionData adminCookie = this.adminSessions.get(sessionId);
		if(adminCookie == null) return false;
		return adminCookie.getExpires() > new Date().getTime();
	}

}
