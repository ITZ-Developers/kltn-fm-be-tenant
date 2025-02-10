package com.tenant.repository;

import com.tenant.model.PaymentPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PaymentPeriodRepository extends JpaRepository<PaymentPeriod, Long>, JpaSpecificationExecutor<PaymentPeriod> {
}