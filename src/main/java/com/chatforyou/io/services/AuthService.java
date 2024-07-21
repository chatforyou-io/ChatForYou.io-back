package com.chatforyou.io.services;

import com.chatforyou.io.models.AdminSessionData;

public interface AuthService {
    boolean isAdminSessionValid(String sessionId);
    void putAdminSession(String sessionId, AdminSessionData data);
    void delAdminSession(String sessionId);
    boolean checkEmailValidate(String token, String code);
}
