package com.kltn.school_hrm.module.payroll.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.kltn.school_hrm.shared.enums.Enums.PaymentMethod;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO ghi nhận đợt thanh toán bảng lương từ bộ phận Tài chính.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollPaymentRequest {

    @NotNull(message = "Số tiền thanh toán không được để trống")
    @DecimalMin(value = "1.0", message = "Số tiền thanh toán phải lớn hơn 0")
    private BigDecimal amount;

    @NotNull(message = "Ngày thanh toán không được để trống")
    private LocalDate paymentDate;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

    private String referenceCode;

    private String note;
}

