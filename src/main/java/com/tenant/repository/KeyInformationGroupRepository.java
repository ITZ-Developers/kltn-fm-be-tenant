package com.tenant.repository;

import com.tenant.model.KeyInformationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface KeyInformationGroupRepository extends JpaRepository<KeyInformationGroup, Long>, JpaSpecificationExecutor<KeyInformationGroup> {
    Optional<KeyInformationGroup> findFirstByName(String name);
}

