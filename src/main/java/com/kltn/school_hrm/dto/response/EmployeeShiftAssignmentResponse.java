package com.kltn.school_hrm.dto.response;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeShiftAssignmentResponse {

    private Long id;

    // Employee info
    private Long employeeId;
    private String employeeCode;
    private String employeeName;

    // Shift info
    private Long shiftId;
    private String shiftCode;
    private String shiftName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean overnight;

    // Assignment info
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private DayOfWeek dayOfWeek;
    private Boolean active;
}
