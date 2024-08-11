package com.chatforyou.io.services;

import com.chatforyou.io.models.AdminSessionData;
import com.chatforyou.io.models.ValidateType;
import com.chatforyou.io.models.out.UserOutVo;
import org.apache.coyote.BadRequestException;

public interface AuthService {
    boolean isAdminSessionValid(String sessionId);
    void putAdminSession(String sessionId, AdminSessionData data);
    void delAdminSession(String sessionId);
    boolean checkEmailValidate(String token, String code);
    UserOutVo getLoginUserInfo(String id, String pwd);

    boolean validateStrByType(ValidateType type, String str)  throws BadRequestException;
}
