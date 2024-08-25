package com.chatforyou.io.repository;

import com.chatforyou.io.entity.ChatRoom;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END FROM ChatRoom r WHERE r.name = :roomName")
    boolean checkExistsByRoomName(@Param("roomName") String name);

    List<ChatRoom> findAllByOrderByCreateDateDesc();
    Optional<ChatRoom> findChatRoomByName(String name);
    Optional<ChatRoom> findChatRoomBySessionId(String sessionId);

}
