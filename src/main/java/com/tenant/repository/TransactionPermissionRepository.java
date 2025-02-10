package com.tenant.repository;

import com.tenant.model.TransactionPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TransactionPermissionRepository extends JpaRepository<TransactionPermission, Long>, JpaSpecificationExecutor<TransactionPermission> {
    Optional<TransactionPermission> findFirstByAccountIdAndTransactionId(Long accountId, Long transactionId);
    Optional<TransactionPermission> findFirstByAccountIdAndTransactionGroupId(Long accountId, Long transactionGroupId);
}
