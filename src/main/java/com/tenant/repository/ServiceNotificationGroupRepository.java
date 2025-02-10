package com.tenant.repository;

import com.tenant.model.ServiceNotificationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ServiceNotificationGroupRepository extends JpaRepository<ServiceNotificationGroup, Long>, JpaSpecificationExecutor<ServiceNotificationGroup> {
    Optional<ServiceNotificationGroup> findFirstByServiceIdAndNotificationGroupId(Long serviceId, Long notificationGroupId);
}
