package com.chatforyou.io.services.impl;

import ch.qos.logback.core.util.StringUtil;
import com.chatforyou.io.entity.SocialUser;
import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.DataType;
import com.chatforyou.io.models.JwtPayload;
import com.chatforyou.io.models.RedisIndex;
import com.chatforyou.io.models.ValidateType;
import com.chatforyou.io.models.in.UserInVo;
import com.chatforyou.io.models.in.UserUpdateVo;
import com.chatforyou.io.models.out.UserOutVo;
import com.chatforyou.io.repository.SocialRepository;
import com.chatforyou.io.repository.UserRepository;
import com.chatforyou.io.services.AuthService;
import com.chatforyou.io.services.UserService;
import com.chatforyou.io.utils.JsonUtils;
import com.chatforyou.io.utils.RedisUtils;
import io.github.dengliming.redismodule.redisearch.index.Document;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final AuthService authService;
    private final RedisUtils redisUtils;
    private final SocialRepository socialRepository;

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
    @Transactional
    public UserOutVo updateUser(UserUpdateVo userUpdateVo, JwtPayload jwtPayload) throws BadRequestException {
        if (!Objects.equals(userUpdateVo.getIdx(), jwtPayload.getIdx())) {
            throw new BadRequestException("The user ID in the token does not match the user ID provided in the chat room information.");
        }

        User user = userRepository.findUserByIdx(userUpdateVo.getIdx())
                .orElseThrow(()->new EntityNotFoundException("can not find user"));

        User updatedUser = User.ofUpdate(userUpdateVo, user);
        return UserOutVo.of(userRepository.saveAndFlush(updatedUser), false);
    }

    @Override
    @Transactional
    public UserOutVo updateUserPwd(UserUpdateVo userUpdateVo, JwtPayload jwtPayload) throws BadRequestException {
        if (!Objects.equals(userUpdateVo.getIdx(), jwtPayload.getIdx())) {
            throw new BadRequestException("The user ID in the token does not match the user ID provided in the chat room information.");
        }

        User user = userRepository.findUserByIdx(userUpdateVo.getIdx())
                .orElseThrow(()->new EntityNotFoundException("can not find user"));

        if (user.getPwd().equals(userUpdateVo.getNewPwd())) {
            throw new BadRequestException("New password cannot be the same as the old password.");
        }

        if (userUpdateVo.getNewPwd() == null) {
            throw new BadRequestException("New Password is Required");
        }

        User updatedUser = User.builder()
                .idx(user.getIdx())
                .id(user.getId())
                .name(user.getName())
                .pwd(userUpdateVo.getNewPwd())
                .usePwd(user.getUsePwd())
                .nickName(user.getNickName())
                .createDate(user.getCreateDate())
                .build();

        return UserOutVo.of(userRepository.saveAndFlush(updatedUser), false);
    }

    @Override
    @Transactional
    public void deleteUser(UserInVo userInVO, JwtPayload jwtPayload) throws BadRequestException {

        if (!Objects.equals(userInVO.getIdx(), jwtPayload.getIdx())) {
            throw new BadRequestException("The user ID in the token does not match the user ID provided in the chat room information.");
        }

        User user = userRepository.findUserByIdx(userInVO.getIdx())
                .orElseThrow(() -> new EntityNotFoundException("can not find user"));

        try {
            // 소셜 유저의 경우 cascade 설정이 되어있어 user 삭제 후 함께 삭제처리
            userRepository.delete(user);

        } catch (Exception e) {
            throw new BadRequestException("can not delete user");
        }
    }

    @Override
    public List<UserOutVo> getLoginUserList(String keyword, int pageNum, int pageSize) {
        List<UserOutVo> userList = new ArrayList<>();
        pageNum = pageNum !=0 ? pageNum - 1 : pageNum;
            List<Document> documents = redisUtils.searchByKeyword(RedisIndex.LOGIN_USER, keyword, pageNum, pageSize);
            for (Document document : documents) {
                Object loginUser = document.getFields().get(DataType.LOGIN_USER.getType());
            if (Objects.isNull(loginUser)) {
                continue;
            }
            UserOutVo user = JsonUtils.jsonToObj(loginUser.toString(), UserOutVo.class);
            userList.add(user);
        }

        return userList;
    }

    @Override
    public List<UserOutVo> getUserList(String keyword, int pageNum, int pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize, Sort.by("nickName").ascending());
        Page<User> userPage;
        if (StringUtil.isNullOrEmpty(keyword)) {
            userPage = userRepository.searchUserList(pageRequest);
        } else {
            userPage = userRepository.searchUserListByKeyword(keyword, pageRequest);
        }

        if (userPage.isEmpty()) {
            return Collections.emptyList();
        }

        return userPage.getContent()
                .stream()
                .map(user -> UserOutVo.of(user, false))
                .toList();
    }

    @Override
    public void getFriendInfo() {

    }

}
