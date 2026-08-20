package com.kltn.school_hrm.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

import com.kltn.school_hrm.enums.Enums.AttendanceStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceCreateRequest {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Work date is required")
    private LocalDate workDate;

    private LocalTime checkIn;

    private LocalTime checkOut;

    private String deviceId;

    private AttendanceStatus status;
}
