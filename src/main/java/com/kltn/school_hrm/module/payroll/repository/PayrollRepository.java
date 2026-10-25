package com.kltn.school_hrm.module.payroll.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.payroll.entity.Payroll;
import com.kltn.school_hrm.shared.enums.Enums.PayrollStatus;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    List<Payroll> findByEmployeeId(Long employeeId);

    List<Payroll> findByPeriodId(Long periodId);

    Optional<Payroll> findByPeriodIdAndEmployeeId(Long periodId, Long employeeId);

    boolean existsByPeriodIdAndEmployeeId(Long periodId, Long employeeId);

    List<Payroll> findByStatus(PayrollStatus status);

    @Query("""
            SELECT p FROM Payroll p
            WHERE p.period.month = :month AND p.period.year = :year
            """)
    List<Payroll> findByMonthAndYear(@Param("month") Integer month, @Param("year") Integer year);
}
