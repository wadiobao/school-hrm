package com.kltn.school_hrm.dto.request;

import java.math.BigDecimal;
import java.util.List;

import com.kltn.school_hrm.enums.Enums.PayrollStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollCreateRequest {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Month is required")
    @Min(1) @Max(12)
    private Integer month;

    @NotNull(message = "Year is required")
    @Min(2000)
    private Integer year;

    private BigDecimal grossSalary;
    private BigDecimal totalWorkDays;
    private BigDecimal totalTeachingHours;
    private BigDecimal totalAllowances;
    private BigDecimal totalDeductions;
    private BigDecimal netSalary;

    private PayrollStatus status;

    /** Danh sách chi tiết các khoản lương */
    private List<PayrollDetailItem> details;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PayrollDetailItem {
        @NotNull
        private Long componentId;
        @NotNull
        private BigDecimal amount;
    }
}
