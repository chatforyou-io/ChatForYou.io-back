package com.chatforyou.io.services.impl;

import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.ValidateType;
import com.chatforyou.io.models.in.UserVO;
import com.chatforyou.io.models.out.UserInfo;
import com.chatforyou.io.repository.UserRepository;
import com.chatforyou.io.services.UserService;
import com.chatforyou.io.utils.AuthUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserInfo getUserByIdx(Long idx) {
//        Optional<User> user = ;
        User user = userRepository.findUserByIdx(idx).orElseThrow(() -> new EntityNotFoundException("can not find user"));
        return UserInfo.of(user);
    }

    @Override
    public UserInfo findUserById(String id) {
        User user = userRepository.findUserById(id).orElseThrow(() -> new EntityNotFoundException("can not find user"));
        return UserInfo.of(user);
    }

    @Override
    public boolean validateStrByType(ValidateType type, String str) throws BadRequestException {
        switch (type) {
            case ID:
                return userRepository.checkExistsById(str);
            case NICKNAME:
                return userRepository.checkExistsByNickName(str);
            case PASSWORD:
                // passwd validate 체크 필요
                break;
        }

        return false;
    }

    @Override
    public UserInfo saveUser(UserVO userVO) throws BadRequestException {
        if (Objects.isNull(userVO.getPwd()) || Objects.isNull(userVO.getConfirmPwd())) {
            throw new BadRequestException("can not confirm user Password");
        }
        if (!userVO.getPwd().equals(userVO.getConfirmPwd())) {
            throw new BadRequestException("can not confirm user Password");
        }

        if (this.validateStrByType(ValidateType.ID, userVO.getId())) {
            throw new BadRequestException("Already Exist User ID");
        }
        User userEntity = User.of(userVO);
        return UserInfo.of(userRepository.saveAndFlush(userEntity));
    }

    @Override
    public UserInfo updateUser(UserVO userVO) throws BadRequestException {
        if (!userVO.getPwd().equals(userVO.getConfirmPwd())) {
            throw new BadRequestException("can not confirm user Password");
        }
        Optional<User> user = userRepository.findUserById(userVO.getId());
        if (user.isEmpty()) {
            throw new EntityNotFoundException("can not find user");
        }

        User updatedUser = User.ofUpdated(userVO, user.get());
        return UserInfo.of(userRepository.saveAndFlush(updatedUser));
    }

    @Override
    public boolean deleteUser(UserVO userVO) throws BadRequestException {
        User user = userRepository.findUserById(userVO.getId())
                .orElseThrow(() -> new EntityNotFoundException("can not find user"));
        if (!user.getPwd().equals(userVO.getPwd())) {
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
    public UserInfo getUserInfo(String id, String passwd) {
        User user = userRepository.findByIdAndPwd(id, AuthUtils.getEncodeStr(passwd))
                .orElseThrow(() -> new EntityNotFoundException("Invalid User Id or Password"));
        return UserInfo.of(user);
    }
}
