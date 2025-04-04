package com.tenant.storage.tenant.repository;

import com.tenant.storage.tenant.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MessageRepository extends JpaRepository<Message, Long>, JpaSpecificationExecutor<Message> {
    void deleteAllByChatRoomId(Long id);
}