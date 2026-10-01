package com.kltn.school_hrm.module.leave.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kltn.school_hrm.module.leave.entity.LeaveBalanceTransaction;

public interface LeaveBalanceTransactionRepository extends JpaRepository<LeaveBalanceTransaction, Long> {

}
