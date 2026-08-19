package com.kltn.school_hrm.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

import com.kltn.school_hrm.enums.status.AttendanceStatus;

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
    private LocalDate workDate;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private String deviceId;
    private AttendanceStatus status;
}
