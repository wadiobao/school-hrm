package com.kltn.school_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kltn.school_hrm.entity.attendance.LeaveBalanceTransaction;

public interface LeaveBalanceTransactionRepository extends JpaRepository<LeaveBalanceTransaction, Long> {

}
