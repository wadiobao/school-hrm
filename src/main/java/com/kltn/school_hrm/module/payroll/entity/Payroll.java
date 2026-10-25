package com.kltn.school_hrm.module.payroll.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.kltn.school_hrm.shared.entity.base.BaseEntity;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.shared.enums.Enums.PayrollStatus;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Bảng lương của một nhân viên trong một kỳ lương.
 * Unique constraint (period, employee) chống tạo trùng (req 5.1).
 * Snapshot fields lưu căn cứ tính lương để không bị ảnh hưởng khi dữ liệu gốc thay đổi (req 5.2).
 */
@Entity
@Table(
        name = "payrolls",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payroll_period_employee",
                columnNames = {"payroll_period_id", "employee_id"}
        ),
        indexes = {
                @Index(name = "idx_payroll_period", columnList = "payroll_period_id"),
                @Index(name = "idx_payroll_employee", columnList = "employee_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Payroll extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_period_id", nullable = false)
    private PayrollPeriod period;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // ── Snapshot: dữ liệu được copy tại thời điểm tính lương (req 5.2) ──
    /** Tên nhân viên tại thời điểm tính — bất biến sau khi lưu */
    @Column(name = "snapshot_employee_name", length = 100)
    private String snapshotEmployeeName;

    /** Mức lương hợp đồng áp dụng */
    @Column(name = "snapshot_contract_salary", precision = 15, scale = 2)
    private BigDecimal snapshotContractSalary;

    /** Số ngày công chuẩn của kỳ */
    @Column(name = "snapshot_standard_days")
    private Integer snapshotStandardDays;

    // ── Kết quả tính lương ──
    /** Số ngày công thực tế được trả lương */
    @Column(name = "paid_work_days", precision = 4, scale = 1)
    private BigDecimal paidWorkDays;

    @Column(name = "total_teaching_hours", precision = 5, scale = 1)
    private BigDecimal totalTeachingHours;

    /** Gross = lương theo công + OT + phụ cấp + thu nhập khác */
    @Column(name = "gross_salary", precision = 15, scale = 2)
    private BigDecimal grossSalary;

    /** Tổng khấu trừ (thuế + bảo hiểm + khác) */
    @Column(name = "total_deductions", precision = 15, scale = 2)
    private BigDecimal totalDeductions;

    /** Net = Gross - totalDeductions */
    @Column(name = "net_salary", precision = 15, scale = 2)
    private BigDecimal netSalary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayrollStatus status;

    @Column(columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "payroll", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PayrollItem> items = new ArrayList<>();

    // ── Domain logic ──

    /** Chỉ được sửa khi chưa PAID */
    public void guardEditable() {
        if (this.status == PayrollStatus.PAID) {
            throw new BusinessException("Bảng lương đã chi trả, không thể sửa");
        }
    }

    /** Approve bảng lương */
    public void approve() {
        if (this.status != PayrollStatus.DRAFT) {
            throw new BusinessException("Chỉ bảng lương DRAFT mới có thể duyệt");
        }
        this.status = PayrollStatus.APPROVED;
    }

    /** Đánh dấu đã chi trả */
    public void markPaid() {
        if (this.status != PayrollStatus.APPROVED) {
            throw new BusinessException("Chỉ bảng lương APPROVED mới có thể đánh dấu đã trả");
        }
        this.status = PayrollStatus.PAID;
    }
}