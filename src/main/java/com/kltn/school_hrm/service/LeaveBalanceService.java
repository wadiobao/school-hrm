package com.kltn.school_hrm.service;

import java.math.BigDecimal;

import com.kltn.school_hrm.entity.attendance.LeaveRequest;
import com.kltn.school_hrm.entity.employee.Employee;

public interface LeaveBalanceService {

    void reserve(Employee employee, int year, BigDecimal days);

    void consume(Employee employee, int year, BigDecimal days);

    void release(Employee employee, int year, BigDecimal days);
}

