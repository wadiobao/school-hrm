package com.kltn.school_hrm.module.leave.service;

import java.math.BigDecimal;

import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.leave.entity.LeaveRequest;

public interface LeaveBalanceService {

    void reserve(Employee employee, int year, BigDecimal days);

    void consume(Employee employee, int year, BigDecimal days, LeaveRequest leaveRequest);

    void release(Employee employee, int year, BigDecimal days);

    void adjust(Employee employee, int year, BigDecimal days, String reason);

    void expireYear(int year);

    void accrueAnnualLeaveForYear(int year, BigDecimal days);

    /**
     * Tự động khởi tạo quỹ nghỉ phép năm cho nhân viên mới tạo (mặc định 12-14 ngày/năm).
     * @param employee Nhân viên vừa được tạo
     * @param year Năm áp dụng
     * @param days Số ngày phép được cấp ban đầu (nếu null, mặc định 12 ngày)
     */
    void initializeBalanceForEmployee(Employee employee, int year, BigDecimal days);

    /** Lấy chi tiết quỹ phép của nhân viên theo năm. */
    com.kltn.school_hrm.module.leave.dto.response.LeaveBalanceResponse getBalanceByEmployeeAndYear(Long employeeId, int year);

    /** Lấy danh sách quỹ phép của tất cả nhân viên trong năm. */
    java.util.List<com.kltn.school_hrm.module.leave.dto.response.LeaveBalanceResponse> getAllBalancesByYear(int year);
}
