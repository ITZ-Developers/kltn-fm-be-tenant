package com.tenant.repository;

import com.tenant.model.ServicePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ServicePermissionRepository extends JpaRepository<ServicePermission, Long>, JpaSpecificationExecutor<ServicePermission> {
    Optional<ServicePermission> findFirstByAccountIdAndServiceId(Long accountId, Long serviceId);
    Optional<ServicePermission> findFirstByAccountIdAndServiceGroupId(Long accountId, Long serviceGroupId);
}
