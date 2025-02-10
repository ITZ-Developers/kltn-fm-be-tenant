package com.tenant.repository;

import com.tenant.model.UserGroupNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserGroupNotificationRepository extends JpaRepository<UserGroupNotification, Long>, JpaSpecificationExecutor<UserGroupNotification> {
    Optional<UserGroupNotification> findFirstByAccountIdAndNotificationGroupId(Long accountId, Long notificationGroupId);
}
