package com.tenant.storage.tenant.repository;

import com.tenant.storage.tenant.model.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long>, JpaSpecificationExecutor<ChatRoomMember> {
    List<ChatRoomMember> findAllByChatRoomId(Long chatRoomId);

    void deleteAllByChatRoomId(Long id);
}
