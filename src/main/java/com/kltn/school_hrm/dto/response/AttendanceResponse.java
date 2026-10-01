package com.kltn.school_hrm.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.kltn.school_hrm.shared.enums.Enums.AttendanceStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponse {
    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private LocalDate workDate;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String deviceId;
    private AttendanceStatus status;
    private Integer lateMinutes;
    private Integer earlyLeaveMinutes;
}

