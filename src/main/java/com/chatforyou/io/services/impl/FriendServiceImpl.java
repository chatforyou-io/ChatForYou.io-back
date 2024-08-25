package com.chatforyou.io.services.impl;

import com.chatforyou.io.entity.User;
import com.chatforyou.io.repository.UserRepository;
import com.chatforyou.io.services.FriendService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        return userRepository.friendListByUserIdx(user.getIdx());
    }
}
