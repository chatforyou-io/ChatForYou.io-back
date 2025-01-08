package com.chatforyou.io.repository;

import com.chatforyou.io.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByIdx(Long idx);
    Optional<User> findByIdAndPwd(String id, String pwd);
    Optional<User> findUserById(String id);
    Optional<User> findUserByNickName(String nickName);

    @Transactional
    @Modifying
    @Query("update User u set u.nickName=:nickName, u.pwd=:pwd where u.id=:id")
    int updateUserById(String id, String nickName, String pwd);

    @Transactional
    @Modifying
    @Query("delete from User u where u.idx=:idx and u.id=:id")
    int deleteUserByIdxAndId(Long idx, String id);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE u.nickName = :nickName")
    boolean checkExistsByNickName(@Param("nickName") String nickName);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE u.id = :id")
    boolean checkExistsById(@Param("id") String id);

    @Query("SELECT u FROM User u WHERE u.nickName LIKE %:keyword% OR u.id LIKE %:keyword%")
    Page<User> searchUserListByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u FROM User u")
    Page<User> searchUserList(@Param("keyword") String keyword, Pageable pageable);

//    @Query("SELECT u from User u join Friend f on u.idx = f.friendIdx where f.userIdx=:idx")
//    List<User> friendListByUserIdx(Long idx);
}