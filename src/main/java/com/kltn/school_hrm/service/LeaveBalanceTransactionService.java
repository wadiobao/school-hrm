package com.kltn.school_hrm.service;

import java.math.BigDecimal;

import com.kltn.school_hrm.entity.attendance.LeaveBalance;
import com.kltn.school_hrm.entity.attendance.LeaveBalanceTransaction;
import com.kltn.school_hrm.entity.attendance.LeaveRequest;

public interface LeaveBalanceTransactionService {

    LeaveBalanceTransaction createApprovedLeaveBalanceTransaction(LeaveBalance leaveBalance, BigDecimal days,
            LeaveRequest leaveRequest);

    LeaveBalanceTransaction createAccrualTransaction(LeaveBalance leaveBalance, BigDecimal days, String reason);

    LeaveBalanceTransaction createAdjustmentTransaction(LeaveBalance leaveBalance, BigDecimal days, String reason);

    LeaveBalanceTransaction createExpirationTransaction(LeaveBalance leaveBalance, BigDecimal days, String reason);

}
