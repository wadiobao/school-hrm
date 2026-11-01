package com.kltn.school_hrm.module.payroll.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.kltn.school_hrm.shared.entity.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Đóng băng toàn bộ dữ liệu đầu vào và kết quả tính toán chi tiết tại thời điểm chốt LOCKED (req 5 & 6).
 * Đảm bảo dữ liệu đã chốt là bất biến và có thể đối soát nguyên trạng ngay cả khi hồ sơ nhân viên thay đổi.
 */
@Entity
@Table(
        name = "payroll_snapshots",
        indexes = {
                @Index(name = "idx_payroll_snapshots_payroll_id", columnList = "payroll_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PayrollSnapshot extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_id", nullable = false, unique = true)
    private Payroll payroll;

    @Column(name = "snapshot_employee_name", length = 100)
    private String snapshotEmployeeName;

    @Column(name = "snapshot_contract_salary", precision = 15, scale = 2)
    private BigDecimal snapshotContractSalary;

    @Column(name = "snapshot_standard_days")
    private Integer snapshotStandardDays;

    @Column(name = "snapshot_paid_work_days", precision = 4, scale = 1)
    private BigDecimal snapshotPaidWorkDays;

    @Column(name = "snapshot_total_teaching_hours", precision = 5, scale = 1)
    private BigDecimal snapshotTotalTeachingHours;

    @Column(name = "snapshot_unpaid_leave_days", precision = 4, scale = 1)
    private BigDecimal snapshotUnpaidLeaveDays;

    @Column(name = "snapshot_taxable_income", precision = 15, scale = 2)
    private BigDecimal snapshotTaxableIncome;

    @Column(name = "policy_version", length = 50)
    private String policyVersion;

    @Column(name = "snapshot_gross_salary", precision = 15, scale = 2)
    private BigDecimal snapshotGrossSalary;

    @Column(name = "snapshot_total_deductions", precision = 15, scale = 2)
    private BigDecimal snapshotTotalDeductions;

    @Column(name = "snapshot_net_salary", precision = 15, scale = 2)
    private BigDecimal snapshotNetSalary;

    /** Tóm tắt các khoản thu nhập/khấu trừ dạng JSON tại thời điểm chốt */
    @Column(name = "items_summary_json", columnDefinition = "TEXT")
    private String itemsSummaryJson;

    @Builder.Default
    @Column(name = "locked_at", nullable = false)
    private LocalDateTime lockedAt = LocalDateTime.now();

    @Column(name = "locked_by_user_id")
    private Long lockedByUserId;

    @Column(name = "locked_by_name", length = 100)
    private String lockedByName;
}

