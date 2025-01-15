package com.chatforyou.io.services;

import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.JwtPayload;
import com.chatforyou.io.models.in.UserInVo;
import com.chatforyou.io.models.in.UserUpdateVo;
import com.chatforyou.io.models.out.UserOutVo;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {
    UserOutVo findUserByIdx(Long idx);
    UserOutVo findUserById(String id);
    UserOutVo saveUser(UserInVo user) throws BadRequestException;
    UserOutVo updateUser(UserUpdateVo user, JwtPayload jwtPayload) throws BadRequestException;
    UserOutVo updateUserPwd(UserUpdateVo user, JwtPayload jwtPayload) throws BadRequestException;
    void deleteUser(UserInVo userInVO, JwtPayload jwtPayload) throws BadRequestException;
    List<UserOutVo> getLoginUserList(String keyword, int pageNum, int pageSize);
    List<UserOutVo> getUserList(String keyword, int pageNum, int pageSize);
    void getFriendInfo();

}
