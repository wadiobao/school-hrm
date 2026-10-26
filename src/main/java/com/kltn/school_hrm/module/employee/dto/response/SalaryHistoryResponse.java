package com.kltn.school_hrm.module.employee.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.kltn.school_hrm.shared.enums.Enums.Currency;
import com.kltn.school_hrm.shared.enums.Enums.SalaryChangeReason;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO phản hồi lịch sử diễn biến điều chỉnh mức lương của hợp đồng.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryHistoryResponse {

    private Long id;
    private Long contractId;
    private String contractNumber;

    private Long employeeId;
    private String employeeName;
    private String employeeCode;

    private BigDecimal oldGrossSalary;
    private BigDecimal newGrossSalary;
    private BigDecimal difference; // newGrossSalary - oldGrossSalary

    private Currency currency;
    private LocalDate effectiveDate;
    private String decisionNumber;

    private SalaryChangeReason reason;
    private String reasonLabel;
    private String note;

    private Long approvedById;
    private String approvedByName;

    private LocalDateTime createdAt;
}
