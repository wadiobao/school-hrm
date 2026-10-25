package com.kltn.school_hrm.module.payroll.entity;

import java.math.BigDecimal;

import com.kltn.school_hrm.shared.entity.base.BaseEntity;
import com.kltn.school_hrm.shared.enums.Enums.PayrollItemType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Ghi nhận từng khoản thu nhập / khấu trừ trong bảng lương.
 * <p>
 * Quy ước dấu amount:
 * - Earning (BASE_SALARY, OVERTIME, ALLOWANCE, OTHER_EARNING): amount > 0
 * - Deduction (TAX, INSURANCE, OTHER_DEDUCTION): amount > 0 (giá trị khấu trừ dương)
 *   → netSalary = sum(earnings) - sum(deductions)
 * </p>
 */
@Entity
@Table(name = "payroll_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PayrollItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_id", nullable = false)
    private Payroll payroll;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PayrollItemType type;

    /** Mô tả chi tiết khoản (ví dụ: "Lương 20/22 ngày công") */
    @Column(length = 255)
    private String description;

    /** Số tiền — luôn dương, chiều trừ xác định bởi type */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
}
