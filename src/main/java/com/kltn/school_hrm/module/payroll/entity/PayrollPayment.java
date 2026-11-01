package com.kltn.school_hrm.module.payroll.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.kltn.school_hrm.shared.entity.base.BaseEntity;
import com.kltn.school_hrm.shared.enums.Enums.PaymentMethod;
import com.kltn.school_hrm.shared.enums.Enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Ghi nhận thông tin các đợt thanh toán bảng lương từ bộ phận Tài chính/Kế toán (req 5 & 7).
 * Hỗ trợ thanh toán từng phần (installments) và xác thực hoàn tất trước khi bảng lương chuyển sang PAID.
 */
@Entity
@Table(
        name = "payroll_payments",
        indexes = {
                @Index(name = "idx_payroll_payments_payroll_id", columnList = "payroll_id"),
                @Index(name = "idx_payroll_payments_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PayrollPayment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_id", nullable = false)
    private Payroll payroll;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Column(name = "reference_code", length = 100)
    private String referenceCode;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.COMPLETED;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "paid_by_user_id")
    private Long paidByUserId;

    @Column(name = "paid_by_name", length = 100)
    private String paidByName;
}

