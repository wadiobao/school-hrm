package com.kltn.school_hrm.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.kltn.school_hrm.enums.Enums.ComponentType;
import com.kltn.school_hrm.enums.Enums.PayrollStatus;

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
    private Long employeeId;
    private Integer month;
    private Integer year;
    private BigDecimal grossSalary;
    private BigDecimal totalWorkDays;
    private BigDecimal totalTeachingHours;
    private BigDecimal totalAllowances;
    private BigDecimal totalDeductions;
    private BigDecimal netSalary;
    private PayrollStatus status;
    private LocalDateTime createdAt;
    private List<PayrollDetailResponse> details;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PayrollDetailResponse {
        private Long id;
        private Long componentId;
        private String componentCode;
        private String componentName;
        private ComponentType componentType;
        private BigDecimal amount;
    }
}
