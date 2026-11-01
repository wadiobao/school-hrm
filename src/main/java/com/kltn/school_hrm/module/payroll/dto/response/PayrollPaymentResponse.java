package com.kltn.school_hrm.module.payroll.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.kltn.school_hrm.shared.enums.Enums.PaymentMethod;
import com.kltn.school_hrm.shared.enums.Enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO trả về thông tin đợt chi trả bảng lương.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollPaymentResponse {

    private Long id;
    private Long payrollId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private String paymentMethodLabel;
    private String referenceCode;
    private PaymentStatus status;
    private String statusLabel;
    private String note;
    private Long paidByUserId;
    private String paidByName;
}

