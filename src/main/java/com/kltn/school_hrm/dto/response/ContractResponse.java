package com.kltn.school_hrm.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonValue;
import com.kltn.school_hrm.enums.Enums.ContractStatus;
import com.kltn.school_hrm.enums.Enums.ContractType;
import com.kltn.school_hrm.enums.Enums.Currency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractResponse {

    private Long id;
    private String contractNumber;

    // Thông tin ngắn gọn của nhân viên sở hữu hợp đồng
    private EmployeeSummary employee;

    private String positionName;

    private LocalDate startDate;
    private LocalDate endDate;
    private ContractType type;
    private ContractStatus status;

    private BigDecimal grossSalary;

    private Currency currency; // Trả về dạng String "VND" hoặc "USD"

    private BigDecimal housingAllowance;
    private BigDecimal flightAllowance;
    private BigDecimal relocationAllowance;

    // Các trường từ BaseEntity
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmployeeSummary {
        private Long id;
        private String employeeCode;
        private String fullName;
        private String email;
    }
}
