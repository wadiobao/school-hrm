package com.kltn.school_hrm.module.leave.service;

import java.math.BigDecimal;

import com.kltn.school_hrm.module.leave.entity.LeaveBalance;
import com.kltn.school_hrm.module.leave.entity.LeaveBalanceTransaction;
import com.kltn.school_hrm.module.leave.entity.LeaveRequest;

public interface LeaveBalanceTransactionService {

    LeaveBalanceTransaction createApprovedLeaveBalanceTransaction(LeaveBalance leaveBalance, BigDecimal days,
            LeaveRequest leaveRequest);

    LeaveBalanceTransaction createAccrualTransaction(LeaveBalance leaveBalance, BigDecimal days, String reason);

    LeaveBalanceTransaction createAdjustmentTransaction(LeaveBalance leaveBalance, BigDecimal days, String reason);

    LeaveBalanceTransaction createExpirationTransaction(LeaveBalance leaveBalance, BigDecimal days, String reason);

}
