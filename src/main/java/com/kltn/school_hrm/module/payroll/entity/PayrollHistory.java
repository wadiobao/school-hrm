package com.kltn.school_hrm.module.payroll.entity;

import java.time.LocalDateTime;

import com.kltn.school_hrm.shared.entity.base.BaseEntity;
import com.kltn.school_hrm.shared.enums.Enums.PayrollStatus;

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
 * Lưu vết lịch sử chuyển đổi trạng thái của bảng lương (req 5).
 * Phục vụ audit, đối soát các lần phê duyệt, khóa sổ hoặc hoàn trả.
 */
@Entity
@Table(
        name = "payroll_histories",
        indexes = {
                @Index(name = "idx_payroll_histories_payroll_id", columnList = "payroll_id"),
                @Index(name = "idx_payroll_histories_changed_at", columnList = "changed_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PayrollHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_id", nullable = false)
    private Payroll payroll;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private PayrollStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private PayrollStatus toStatus;

    @Column(name = "changed_by_user_id")
    private Long changedByUserId;

    @Column(name = "changed_by_name", length = 100)
    private String changedByName;

    @Builder.Default
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt = LocalDateTime.now();

    @Column(name = "reason", length = 500)
    private String reason;

    /**
     * Factory tạo bản ghi lịch sử trạng thái
     */
    public static PayrollHistory of(
            Payroll payroll,
            PayrollStatus fromStatus,
            PayrollStatus toStatus,
            Long changedByUserId,
            String changedByName,
            String reason
    ) {
        return PayrollHistory.builder()
                .payroll(payroll)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedByUserId(changedByUserId)
                .changedByName(changedByName)
                .changedAt(LocalDateTime.now())
                .reason(reason)
                .build();
    }
}

