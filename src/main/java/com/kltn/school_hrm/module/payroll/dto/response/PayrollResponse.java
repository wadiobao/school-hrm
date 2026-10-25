package com.kltn.school_hrm.module.payroll.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.kltn.school_hrm.shared.enums.Enums.PayrollItemType;
import com.kltn.school_hrm.shared.enums.Enums.PayrollStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollResponse {
    private Long id;

    // Period info
    private Long periodId;
    private Integer month;
    private Integer year;

    // Employee info
    private Long employeeId;
    private String employeeName;
    private String employeeCode;

    // Snapshot
    private BigDecimal snapshotContractSalary;
    private Integer snapshotStandardDays;
    private BigDecimal paidWorkDays;
    private BigDecimal totalTeachingHours;

    // Results
    private BigDecimal grossSalary;
    private BigDecimal totalDeductions;
    private BigDecimal netSalary;

    private PayrollStatus status;
    private String note;
    private LocalDateTime createdAt;

    private List<PayrollItemResponse> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PayrollItemResponse {
        private Long id;
        private PayrollItemType type;
        private String description;
        private BigDecimal amount;
    }
}
