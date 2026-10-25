package com.kltn.school_hrm.module.payroll.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.kltn.school_hrm.shared.enums.Enums.PayrollPeriodStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollPeriodResponse {
    private Long id;
    private Integer month;
    private Integer year;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer standardWorkDays;
    private PayrollPeriodStatus status;
    private String note;
    private LocalDateTime createdAt;
}
