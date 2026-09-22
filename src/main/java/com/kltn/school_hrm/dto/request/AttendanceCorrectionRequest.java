package com.kltn.school_hrm.dto.request;

import java.time.LocalDateTime;

import com.kltn.school_hrm.enums.Enums.AttendanceStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceCorrectionRequest {

    private LocalDateTime checkIn;

    private LocalDateTime checkOut;

    private AttendanceStatus status;

    private Integer lateMinutes;

    private Integer earlyLeaveMinutes;

    private String reason;
}
