package com.chatforyou.io.services.impl;

import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.SearchType;
import com.chatforyou.io.models.ValidateType;
import com.chatforyou.io.models.in.UserInVo;
import com.chatforyou.io.models.out.UserOutVo;
import com.chatforyou.io.repository.UserRepository;
import com.chatforyou.io.services.AuthService;
import com.chatforyou.io.services.UserService;
import com.chatforyou.io.utils.JsonUtils;
import com.chatforyou.io.utils.RedisUtils;
import io.github.dengliming.redismodule.redisearch.index.Document;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final AuthService authService;
    private final RedisUtils redisUtils;

    private final int MAX_FRIEND_USERS = 50;

    @Override
    public UserOutVo findUserByIdx(Long idx) {
        User user = userRepository.findUserByIdx(idx).orElseThrow(() -> new EntityNotFoundException("can not find user"));
        return UserOutVo.of(user, false);
    }

    @Override
    public UserOutVo findUserById(String id) {
        User user = userRepository.findUserById(id).orElseThrow(() -> new EntityNotFoundException("can not find user"));
        return UserOutVo.of(user, false);
    }

    @Override
    public UserOutVo saveUser(UserInVo userInVO) throws BadRequestException {
        if (Objects.isNull(userInVO.getPwd()) || Objects.isNull(userInVO.getConfirmPwd())) {
            throw new BadRequestException("can not confirm user Password");
        }
        if (!userInVO.getPwd().equals(userInVO.getConfirmPwd())) {
            throw new BadRequestException("can not confirm user Password");
        }

        if (authService.validateStrByType(ValidateType.ID, userInVO.getId())) {
            throw new BadRequestException("Already Exist User ID");
        }
        User userEntity = User.ofSave(userInVO);
        return UserOutVo.of(userRepository.saveAndFlush(userEntity), false);
    }

    @Override
    public UserOutVo updateUser(UserInVo userInVO) throws BadRequestException {
        if (!userInVO.getPwd().equals(userInVO.getConfirmPwd())) {
            throw new BadRequestException("can not confirm user Password");
        }
        User user = userRepository.findUserById(userInVO.getId())
                .orElseThrow(()->new EntityNotFoundException("can not find user"));

        User updatedUser = User.ofUpdate(userInVO, user);
        return UserOutVo.of(userRepository.saveAndFlush(updatedUser), false);
    }

    @Override
    public boolean deleteUser(UserInVo userInVO) throws BadRequestException {
        User user = userRepository.findUserById(userInVO.getId())
                .orElseThrow(() -> new EntityNotFoundException("can not find user"));
        if (!user.getPwd().equals(userInVO.getPwd())) {
            throw new BadRequestException("does not match user pwd");
        }
        int result = userRepository.deleteUserByIdxAndId(user.getIdx(), user.getId());
        if (result > 0) {
            return true;
        } else {
            throw new BadRequestException("can not delete user");
        }
    }

    @Override
    public List<UserOutVo> getUserList(String keyword, int pageNum, int pageSize) {
        List<UserOutVo> userList = new ArrayList<>();
        pageNum = pageNum !=0 ? pageNum - 1 : pageNum;
        List<Document> documents = redisUtils.searchByKeyword(SearchType.LOGIN_USER, keyword, pageNum, pageSize);
        for (Document document : documents) {
            Object loginUser = document.getFields().get("login_user");
            if (Objects.isNull(loginUser)) {
                continue;
            }
            UserOutVo user = JsonUtils.jsonToObj(loginUser.toString(), UserOutVo.class);
            userList.add(user);
        }

        return userList;
    }

    @Override
    public void getFriendInfo() {

    }

}
