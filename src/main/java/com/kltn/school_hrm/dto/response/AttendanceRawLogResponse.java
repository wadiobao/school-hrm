package com.kltn.school_hrm.dto.response;

import java.time.LocalDateTime;

import com.kltn.school_hrm.enums.Enums.AttendanceEventType;
import com.kltn.school_hrm.enums.Enums.AttendanceSource;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceRawLogResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDateTime eventTime;
    private AttendanceEventType eventType;
    private AttendanceSource source;
    private String deviceId;
    private String eventId;
}
