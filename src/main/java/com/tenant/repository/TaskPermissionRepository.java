package com.tenant.repository;

import com.tenant.model.TaskPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TaskPermissionRepository extends JpaRepository<TaskPermission, Long>, JpaSpecificationExecutor<TaskPermission> {
    Optional<TaskPermission> findFirstByAccountIdAndTaskId(Long accountId, Long taskId);
    Optional<TaskPermission> findFirstByAccountIdAndProjectId(Long accountId, Long projectId);
}

