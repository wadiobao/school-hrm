package com.kltn.school_hrm.module.payroll.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.module.payroll.entity.PayrollItem;
import com.kltn.school_hrm.shared.enums.Enums.PayrollItemType;

@Repository
public interface PayrollItemRepository extends JpaRepository<PayrollItem, Long> {

    List<PayrollItem> findByPayrollId(Long payrollId);

    List<PayrollItem> findByPayrollIdAndType(Long payrollId, PayrollItemType type);

    void deleteByPayrollId(Long payrollId);
}
