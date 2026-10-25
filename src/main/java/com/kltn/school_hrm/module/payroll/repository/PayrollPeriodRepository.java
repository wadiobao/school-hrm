package com.kltn.school_hrm.module.payroll.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.payroll.entity.PayrollPeriod;
import com.kltn.school_hrm.shared.enums.Enums.PayrollPeriodStatus;

@Repository
public interface PayrollPeriodRepository extends JpaRepository<PayrollPeriod, Long> {

    Optional<PayrollPeriod> findByMonthAndYear(Integer month, Integer year);

    boolean existsByMonthAndYear(Integer month, Integer year);

    List<PayrollPeriod> findByStatus(PayrollPeriodStatus status);

    List<PayrollPeriod> findAllByOrderByYearDescMonthDesc();
}
