package com.kltn.school_hrm.module.payroll.entity;

import java.time.LocalDate;

import com.kltn.school_hrm.shared.entity.base.BaseEntity;
import com.kltn.school_hrm.shared.enums.Enums.PayrollPeriodStatus;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Quản lý kỳ lương — mỗi kỳ tương ứng một tháng/năm cụ thể.
 * Sau khi LOCKED thì không được tính lại hoặc xóa.
 */
@Entity
@Table(
        name = "payroll_periods",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payroll_period_month_year",
                columnNames = {"month", "year"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PayrollPeriod extends BaseEntity {

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    /** Ngày bắt đầu tính công — có thể khác 1/{month} */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** Ngày kết thúc tính công */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /** Số ngày công chuẩn trong kỳ */
    @Column(name = "standard_work_days", nullable = false)
    private Integer standardWorkDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayrollPeriodStatus status;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    /** Chốt kỳ lương — sau đó không thể sửa payroll trong kỳ này */
    public void lock() {
        if (this.status == PayrollPeriodStatus.LOCKED) {
            throw new BusinessException("Kỳ lương đã được chốt rồi");
        }
        this.status = PayrollPeriodStatus.LOCKED;
    }
}
