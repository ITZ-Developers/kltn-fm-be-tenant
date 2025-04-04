package com.tenant.storage.tenant.repository;

import com.tenant.storage.tenant.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, JpaSpecificationExecutor<ChatRoom> {
}