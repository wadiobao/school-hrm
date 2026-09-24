package com.kltn.school_hrm.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.kltn.school_hrm.enums.Enums.AttendanceStatus;

import lombok.Builder;
import lombok.Data;

/**
 * Response DTO cho AttendanceRecord - dữ liệu chấm công đã được aggregate.
 * Đây là dữ liệu HR/User chủ yếu đọc.
 */
@Data
@Builder
public class AttendanceRecordResponse {
    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private LocalDate workDate;
    private LocalDateTime firstCheckIn;
    private LocalDateTime lastCheckOut;
    private Integer lateMinutes;
    private Integer earlyLeaveMinutes;
    private Integer workedMinutes;
    private AttendanceStatus status;
    private String note;
}
