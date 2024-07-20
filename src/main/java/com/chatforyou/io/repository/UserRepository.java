package com.chatforyou.io.repository;

import com.chatforyou.io.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Transactional
    @Modifying
    @Query("update User u set u.nickName=:nickName, u.pwd=:pwd where u.idx=:idx")
    User updateUserByIdx(String nickName, String pwd, Long idx);

    User getUserByIdx(Long idx);

    @Transactional
    @Query("delete User u where u.idx=:idx and u.id=:id")
    boolean deleteUserByIdxAndId(Long idx, String id);

    User getUserById(String id);

    @Transactional
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE u.nickName = :nickName")
    boolean existsByNickName(@Param("nickName") String nickName);
}