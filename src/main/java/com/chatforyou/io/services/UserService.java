package com.chatforyou.io.services;

import com.chatforyou.io.models.in.UserInVo;
import com.chatforyou.io.models.out.UserOutVo;
import org.apache.coyote.BadRequestException;

public interface UserService {
    UserOutVo getUserByIdx(Long idx);
    UserOutVo findUserById(String id);
    UserOutVo saveUser(UserInVo user) throws BadRequestException;
    UserOutVo updateUser(UserInVo user) throws BadRequestException;
    boolean deleteUser(UserInVo userInVO) throws BadRequestException;
}
