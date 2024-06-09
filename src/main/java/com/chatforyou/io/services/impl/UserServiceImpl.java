package com.chatforyou.io.services.impl;

import com.chatforyou.io.entity.User;
import com.chatforyou.io.models.UserDto;
import com.chatforyou.io.repository.UserRepository;
import com.chatforyou.io.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    @Override
    public UserDto getUserByIdx(Long idx) {
        User user = userRepository.getUserByIdx(idx);
        if (Objects.isNull(user)) {
            throw new EntityNotFoundException();
        }
        return UserDto.of(user);
    }

    @Override
    public UserDto saveUser(String requestBody) {
        // TODO 데이터 파싱
        User user = new User();
        return UserDto.of(userRepository.saveAndFlush(user));
    }
}
