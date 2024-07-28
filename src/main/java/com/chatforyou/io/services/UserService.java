package com.chatforyou.io.services;

import com.chatforyou.io.models.ValidateType;
import com.chatforyou.io.models.in.UserVO;
import com.chatforyou.io.models.out.UserInfo;
import org.apache.coyote.BadRequestException;

public interface UserService {
    UserInfo getUserByIdx(Long idx);
    UserInfo findUserById(String id);
    boolean validateStrByType(ValidateType type, String str) throws BadRequestException;
    UserInfo saveUser(UserVO user) throws BadRequestException;
    UserInfo updateUser(UserVO user) throws BadRequestException;
    boolean deleteUser(UserVO userVO) throws BadRequestException;
    UserInfo getUserInfo(String id, String passwd);
}
