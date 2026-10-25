package com.kltn.school_hrm.module.payroll.dto.request;

import java.math.BigDecimal;
import java.util.List;

import com.kltn.school_hrm.shared.enums.Enums.PayrollItemType;
import com.kltn.school_hrm.shared.enums.Enums.PayrollStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request để tạo hoặc cập nhật thủ công bảng lương của một nhân viên */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollCreateRequest {

    @NotNull(message = "periodId không được để trống")
    private Long periodId;

    @NotNull(message = "employeeId không được để trống")
    private Long employeeId;

    private BigDecimal paidWorkDays;
    private BigDecimal totalTeachingHours;
    private BigDecimal grossSalary;
    private BigDecimal totalDeductions;
    private BigDecimal netSalary;
    private PayrollStatus status;
    private String note;

    /** Danh sách từng khoản lương chi tiết */
    private List<PayrollItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PayrollItemRequest {
        @NotNull
        private PayrollItemType type;
        private String description;
        @NotNull
        private BigDecimal amount;
    }
}
