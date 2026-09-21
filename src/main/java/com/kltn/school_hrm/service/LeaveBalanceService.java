package com.kltn.school_hrm.service;

import java.math.BigDecimal;

import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.entity.leave.LeaveRequest;

public interface LeaveBalanceService {

    void reserve(Employee employee, int year, BigDecimal days);

    void consume(Employee employee, int year, BigDecimal days, LeaveRequest leaveRequest);

    void release(Employee employee, int year, BigDecimal days);

    void adjust(Employee employee, int year, BigDecimal days, String reason);

    void expireYear(int year);

    void accrueAnnualLeaveForYear(int year, BigDecimal days);
}
