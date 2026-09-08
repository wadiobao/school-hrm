package com.kltn.school_hrm.service;

import com.kltn.school_hrm.entity.attendance.LeaveRequest;
import com.kltn.school_hrm.entity.employee.Employee;

public interface LeaveBalanceService {

    public void reserve(Employee employee, int year, int days);

    public void consume(Employee employee, int year, int days);
}
