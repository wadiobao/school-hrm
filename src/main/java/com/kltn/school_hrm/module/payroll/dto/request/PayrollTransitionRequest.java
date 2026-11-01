package com.kltn.school_hrm.module.payroll.dto.request;

import com.kltn.school_hrm.shared.enums.Enums.PayrollStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Yêu cầu chuyển đổi trạng thái của bảng lương.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollTransitionRequest {

    @NotNull(message = "Trạng thái đích không được để trống")
    private PayrollStatus targetStatus;

    /**
     * Lý do chuyển đổi trạng thái (bắt buộc khi hoàn trả hoặc mở khóa LOCKED -> REVIEW).
     */
    private String reason;
}

