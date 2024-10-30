package com.chatforyou.io.services;

import com.chatforyou.io.models.in.UserInVo;
import com.chatforyou.io.models.in.UserUpdateVo;
import com.chatforyou.io.models.out.UserOutVo;
import org.apache.coyote.BadRequestException;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserOutVo findUserByIdx(Long idx);
    UserOutVo findUserById(String id);
    UserOutVo saveUser(UserInVo user) throws BadRequestException;
    UserOutVo updateUser(UserUpdateVo user) throws BadRequestException;
    UserOutVo updateUserPwd(UserUpdateVo user) throws BadRequestException;
    boolean deleteUser(UserInVo userInVO) throws BadRequestException;

    List<UserOutVo> getUserList(String keyword, int pageNum, int pageSize);
    void getFriendInfo();

}
