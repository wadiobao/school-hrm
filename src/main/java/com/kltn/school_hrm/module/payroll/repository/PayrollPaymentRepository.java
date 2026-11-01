package com.kltn.school_hrm.module.payroll.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.payroll.entity.PayrollPayment;
import com.kltn.school_hrm.shared.enums.Enums.PaymentStatus;

@Repository
public interface PayrollPaymentRepository extends JpaRepository<PayrollPayment, Long> {

    List<PayrollPayment> findByPayrollIdOrderByPaymentDateDesc(Long payrollId);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM PayrollPayment p
            WHERE p.payroll.id = :payrollId AND p.status = :status
            """)
    BigDecimal sumAmountByPayrollIdAndStatus(
            @Param("payrollId") Long payrollId,
            @Param("status") PaymentStatus status
    );
}

