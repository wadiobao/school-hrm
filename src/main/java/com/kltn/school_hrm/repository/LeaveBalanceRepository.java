package com.kltn.school_hrm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.kltn.school_hrm.entity.attendance.LeaveBalance;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long>,
        JpaSpecificationExecutor<LeaveBalance> {

    Optional<LeaveBalance> findByEmployeeIdAndYear(Long employeeId, Integer year);

}
