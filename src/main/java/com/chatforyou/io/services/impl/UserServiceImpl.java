package com.chatforyou.io.services.impl;

import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.in.UserVO;
import com.chatforyou.io.models.out.UserInfo;
import com.chatforyou.io.repository.UserRepository;
import com.chatforyou.io.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserInfo getUserByIdx(Long idx) {
        User user = userRepository.getUserByIdx(idx);
        if (Objects.isNull(user)) {
            throw new EntityNotFoundException();
        }
        return UserInfo.of(user);
    }

    @Override
    public UserInfo getUserById(String id) {
        return UserInfo.of(userRepository.getUserById(id));
    }

    @Override
    public boolean checkNickName(String nickName) {
        return userRepository.existsByNickName(nickName);
    }

    @Override
    public UserInfo saveUser(UserVO userVo) {
//        User user = new User();
        User userEntity = User.of(userVo);
        return UserInfo.of(userRepository.saveAndFlush(userEntity));
    }

    @Override
    public UserInfo updateUser(UserVO userVO) {

        User user = userRepository.updateUserByIdx(userVO.getNickName(), userVO.getPwd(), userVO.getIdx());
        return UserInfo.of(user);
    }

    @Override
    public boolean deleteUser(UserVO userVO) {
        return userRepository.deleteUserByIdxAndId(userVO.getIdx(), userVO.getId());
    }
}
