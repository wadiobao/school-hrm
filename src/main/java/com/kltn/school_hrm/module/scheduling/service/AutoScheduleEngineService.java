package com.kltn.school_hrm.module.scheduling.service;

import com.kltn.school_hrm.module.scheduling.dto.request.AutoScheduleSettingRequest;
import com.kltn.school_hrm.module.scheduling.dto.response.AutoScheduleResultResponse;

/**
 * Động cơ tự động phân bổ giáo viên theo công thức cân bằng tải
 * và tự động xếp thời khóa biểu cho toàn bộ các lớp trong học kỳ.
 */
public interface AutoScheduleEngineService {

    /**
     * Thực thi thuật toán 2 pha:
     * 1. Quy đổi tỉ lệ môn học -> số tiết/tuần (Hamilton Largest Remainder) & Tự động phân công giáo viên cân bằng tải.
     * 2. Xếp lịch tự động không xung đột (CSP + MRV Heuristic + Kempe Swap Repair) cho tất cả các lớp trong học kỳ.
     */
    AutoScheduleResultResponse generateSemesterSchedule(AutoScheduleSettingRequest request);
}
