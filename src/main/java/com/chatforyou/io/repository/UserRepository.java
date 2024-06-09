package com.chatforyou.io.repository;

import com.chatforyou.io.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User getUserByIdx(Long idx);
    int deleteUserByIdxAndId(Long idx, String id);
}