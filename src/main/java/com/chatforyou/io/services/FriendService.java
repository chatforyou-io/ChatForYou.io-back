package com.chatforyou.io.services;

import com.chatforyou.io.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

public interface FriendService {
    List<User> getFriendLists(String id);
}
