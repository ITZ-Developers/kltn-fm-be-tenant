package com.tenant.storage.tenant.repository;

import com.tenant.storage.tenant.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SettingRepository extends JpaRepository<Setting, Long>, JpaSpecificationExecutor<Setting> {
    Optional<Setting> findFirstByKeyName(String keyName);
    Optional<Setting> findFirstByGroupNameAndKeyName(String groupName, String keyName);
    @Query("SELECT s FROM Setting s WHERE s.keyName IN :keyNames AND s.isSystem = :isSystem")
    List<Setting> findByKeyNames(@Param("keyNames") List<String> keyNames, @Param("isSystem") Boolean isSystem);
    @Query("SELECT s FROM Setting s WHERE s.groupName IN :groupNames AND s.isSystem = :isSystem")
    List<Setting> findByGroupNames(@Param("groupNames") List<String> groupNames, @Param("isSystem") Boolean isSystem);
    List<Setting> findAllByIsSystem(Boolean isSystem);
}
