package com.tenant.storage.tenant.repository;

import com.tenant.storage.tenant.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long>, JpaSpecificationExecutor<Message> {
    @Transactional
    void deleteAllByChatRoomId(Long id);
    @Modifying
    @Query("UPDATE Message mess SET mess.parent = null " +
            "WHERE mess.parent IS NOT NULL AND mess.chatRoom.id = :chatroomId")
    void updateParentNullByChatRoomId(@Param("chatroomId") Long chatRoomId);

    @Query("SELECT mess FROM Message mess WHERE mess.chatRoom.id = :chatroomId " +
            "AND mess.createdDate = (SELECT MAX(m.createdDate) FROM Message m WHERE m.chatRoom.id = :chatroomId)")
    Message findLastMessageByChatRoomId(@Param("chatroomId") Long chatroomId);

    @Modifying
    @Query("UPDATE Message mess SET mess.parent = null " +
            "WHERE mess.parent IS NOT NULL AND mess.chatRoom.id = :chatroomId AND mess.sender = :memberId")
    void updateParentNullByChatRoomIdAndMemberId(@Param("chatroomId") Long chatRoomId,@Param("memberId") Long memberId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Message mess " +
            "WHERE mess.chatRoom.id = :chatroomId AND mess.sender = :memberId ")
    void deleteAllByChatRoomIdAndMemberId(Long chatroomId, Long memberId);

    Optional<Message> findFirstByIdAndChatRoomId(Long messageId, Long chatroomId);
}