package com.kltn.school_hrm.dto.request;

import java.time.LocalDateTime;

import com.kltn.school_hrm.enums.Enums.AttendanceEventType;
import com.kltn.school_hrm.enums.Enums.AttendanceSource;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO nhận raw attendance event từ mọi nguồn (WEB, MOBILE, FINGERPRINT, ...).
 * Backend xử lý giống nhau bất kể source.
 */
@Data
public class AttendanceRawLogRequest {

    @NotNull(message = "employeeId không được để trống")
    private Long employeeId;

    /** Thời điểm chấm công. Nếu null thì dùng LocalDateTime.now() */
    private LocalDateTime eventTime;

    @NotNull(message = "eventType (IN/OUT) không được để trống")
    private AttendanceEventType eventType;

    @NotNull(message = "source không được để trống")
    private AttendanceSource source;

    /** ID thiết bị - nullable với WEB/MANUAL */
    private String deviceId;

    /**
     * Unique event ID từ thiết bị để chống duplicate khi retry.
     * Nếu null, backend sẽ tự sinh.
     */
    private String eventId;
}
