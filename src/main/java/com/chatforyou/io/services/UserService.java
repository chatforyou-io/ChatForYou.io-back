package com.chatforyou.io.services;

import com.chatforyou.io.models.in.UserVO;
import com.chatforyou.io.models.out.UserInfo;

public interface UserService {
    UserInfo getUserByIdx(Long idx);
    UserInfo getUserById(String id);
    boolean checkNickName(String nickName);
    UserInfo saveUser(UserVO user);

    UserInfo updateUser(UserVO user);

    boolean deleteUser(UserVO userVO);
}
