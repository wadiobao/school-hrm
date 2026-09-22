package com.kltn.school_hrm.service;

import com.kltn.school_hrm.dto.response.AttendanceCalculationResult;
import com.kltn.school_hrm.entity.attendance.Attendance;
import com.kltn.school_hrm.entity.attendance.Shift;

/**
 * Service tính toán kết quả chấm công độc lập với quản lý lifecycle (service.md mục 3.2).
 */
public interface AttendanceCalculationService {

    /**
     * So sánh lịch làm việc chuẩn (Shift) với thời gian thực tế (Attendance)
     * để tính toán số phút đi trễ, về sớm, thời gian làm và trạng thái.
     */
    AttendanceCalculationResult calculate(Attendance attendance, Shift shift);
}
