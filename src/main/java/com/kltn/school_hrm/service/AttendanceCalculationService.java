package com.kltn.school_hrm.service;

import com.kltn.school_hrm.dto.response.AttendanceCalculationResult;
import com.kltn.school_hrm.entity.attendance.Attendance;
import com.kltn.school_hrm.entity.attendance.AttendanceRecord;
import com.kltn.school_hrm.entity.attendance.Shift;

/**
 * Service tính toán kết quả chấm công độc lập với quản lý lifecycle.
 * Theo req.md: Calculation xử lý phía sau Aggregation, không gắn với Controller.
 */
public interface AttendanceCalculationService {

    /**
     * Tính toán từ AttendanceRecord (new architecture: RawLog → Aggregation → Record → Calculation).
     *
     * @param record AttendanceRecord đã aggregate (có firstCheckIn, lastCheckOut)
     * @param shift  Ca làm việc áp dụng
     * @return kết quả tính late/early/worked/status
     */
    AttendanceCalculationResult calculateFromRecord(AttendanceRecord record, Shift shift);

    /**
     * @deprecated Dùng calculateFromRecord(AttendanceRecord, Shift) với kiến trúc mới.
     * Giữ lại để backward compat với code cũ.
     */
    @Deprecated
    AttendanceCalculationResult calculate(Attendance attendance, Shift shift);
}
