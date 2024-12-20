package com.chatforyou.io.services.impl;

import com.chatforyou.io.entity.User;
import com.chatforyou.io.repository.UserRepository;
import com.chatforyou.io.services.FriendService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final UserRepository userRepository;

    @Override
    public List<User> getFriendLists(String id) {
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new EntityNotFoundException("can not find user"));
        // TODO 친구 기능 사용 시 개발 필요
//        return userRepository.friendListByUserIdx(user.getIdx());
        return Collections.emptyList();
    }
}
