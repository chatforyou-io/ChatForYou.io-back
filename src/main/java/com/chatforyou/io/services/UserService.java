package com.chatforyou.io.services;

import com.chatforyou.io.models.UserDto;

public interface UserService {
    UserDto getUserByIdx(Long idx);
    UserDto saveUser(String requestBody);

}
