package com.kltn.school_hrm.service;

import java.time.LocalDate;
import java.util.List;

import com.kltn.school_hrm.dto.response.AttendanceRecordResponse;

/**
 * Quản lý AttendanceRecord - đây là dữ liệu HR/User chủ yếu đọc.
 * Theo req.md section 11: AttendanceRecordController → AttendanceRecordService
 */
public interface AttendanceRecordService {

    /**
     * GET /api/attendance-records/{id}
     */
    AttendanceRecordResponse getById(Long id);

    /**
     * GET /api/employees/{employeeId}/attendance-records
     */
    List<AttendanceRecordResponse> getByEmployee(Long employeeId);

    /**
     * GET /api/attendance-records?from=2026-09-01&to=2026-09-30
     */
    List<AttendanceRecordResponse> getByDateRange(LocalDate from, LocalDate to);

    /**
     * Lấy records của nhân viên theo khoảng ngày
     */
    List<AttendanceRecordResponse> getByEmployeeAndDateRange(Long employeeId, LocalDate from, LocalDate to);

    /**
     * HR thủ công trigger re-aggregate một ngày (chạy lại Aggregation từ raw logs)
     */
    AttendanceRecordResponse reAggregate(Long employeeId, LocalDate workDate);
}
