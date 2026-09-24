package com.kltn.school_hrm.service;

import java.time.LocalDate;

import com.kltn.school_hrm.entity.attendance.AttendanceRecord;

/**
 * Gom các raw logs trong ngày thành AttendanceRecord có ý nghĩa nghiệp vụ.
 * Internal processing - không cần Controller.
 *
 * Flow: RawLog → Aggregation → AttendanceRecord → Calculation
 */
public interface AttendanceAggregationService {

    /**
     * Aggregate raw logs của một nhân viên trong một ngày thành AttendanceRecord.
     * Tìm firstCheckIn (IN đầu tiên) và lastCheckOut (OUT cuối cùng).
     * Gọi AttendanceCalculationService để tính late/early/worked minutes.
     *
     * @param employeeId ID nhân viên
     * @param workDate ngày làm việc cần aggregate
     * @return AttendanceRecord đã được tính toán và lưu
     */
    AttendanceRecord aggregate(Long employeeId, LocalDate workDate);
}
