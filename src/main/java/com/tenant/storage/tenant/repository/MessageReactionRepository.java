package com.tenant.storage.tenant.repository;

import com.tenant.storage.tenant.model.Message;
import com.tenant.storage.tenant.model.MessageReaction;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long>, JpaSpecificationExecutor<MessageReaction> {

    @Transactional
    @Modifying
    @Query("DELETE FROM MessageReaction mr " +
            "WHERE mr.account.id = :accountId AND mr.message.id = :messageId ")
    void deleteAllByAccountIdAndMessageId(@Param("accountId")Long accountId,@Param("messageId") Long messageId);

    @Transactional
    void deleteAllByMessageId(Long messageId);

    @Transactional
    @Modifying
    @Query("DELETE FROM MessageReaction mr WHERE mr.message.chatRoom.id = :chatRoomId")
    void deleteAllByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}
