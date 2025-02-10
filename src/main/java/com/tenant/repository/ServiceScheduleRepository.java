package com.tenant.repository;

import com.tenant.model.ServiceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ServiceScheduleRepository extends JpaRepository<ServiceSchedule, Long>, JpaSpecificationExecutor<ServiceSchedule> {
    @Transactional
    void deleteAllByServiceId(Long serviceId);
}
