package com.kltn.school_hrm.module.payroll.dto.response;

import java.time.LocalDateTime;

import com.kltn.school_hrm.shared.enums.Enums.PayrollStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO trả về lịch sử audit trail chuyển đổi trạng thái của bảng lương.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollHistoryResponse {

    private Long id;
    private Long payrollId;
    private PayrollStatus fromStatus;
    private PayrollStatus toStatus;
    private String fromStatusLabel;
    private String toStatusLabel;
    private Long changedByUserId;
    private String changedByName;
    private LocalDateTime changedAt;
    private String reason;
}

