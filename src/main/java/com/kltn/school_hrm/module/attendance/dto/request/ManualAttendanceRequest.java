package com.kltn.school_hrm.module.attendance.dto.request;

import java.time.LocalDate;

import com.kltn.school_hrm.shared.enums.Enums.AttendanceStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO dùng cho HR ghi nhận/cập nhật chấm công thủ công từ Web UI.
 * Khác với CheckInRequest (device-based), request này nhận trực tiếp
 * thời gian check-in/out và trạng thái dạng HH:mm string.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualAttendanceRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Work date is required")
    private LocalDate workDate;

    /** HH:mm hoặc HH:mm:ss */
    private String checkIn;

    /** HH:mm hoặc HH:mm:ss */
    private String checkOut;

    private AttendanceStatus status;

    /** Mã thiết bị (mặc định WEB_MANUAL nếu null) */
    private String deviceId;
}
