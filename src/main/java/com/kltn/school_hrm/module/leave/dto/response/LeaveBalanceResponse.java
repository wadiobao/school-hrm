package com.kltn.school_hrm.module.leave.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO trả về thông tin Quỹ nghỉ phép của nhân viên:
 * Tổng ngày phép, Đã nghỉ, Đang chờ duyệt, Còn lại, Hết hạn.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalanceResponse {

    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private Integer year;
    private BigDecimal totalDays;
    private BigDecimal usedDays;
    private BigDecimal pendingDays;
    private BigDecimal expiredDays;
    private BigDecimal remainingDays;
}
