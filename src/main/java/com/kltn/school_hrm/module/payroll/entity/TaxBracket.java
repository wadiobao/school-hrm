package com.kltn.school_hrm.module.payroll.entity;

import java.math.BigDecimal;

import com.kltn.school_hrm.shared.entity.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Bậc thuế trong biểu thuế lũy tiến từng phần của chính sách thuế TNCN.
 * Quy định ngưỡng thu nhập tính thuế và thuế suất áp dụng tương ứng.
 */
@Entity
@Table(name = "tax_brackets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TaxBracket extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_policy_id", nullable = false)
    private TaxPolicy taxPolicy;

    /** Thứ tự bậc thuế (1, 2, 3, 4, 5...) */
    @Column(name = "bracket_order", nullable = false)
    private Integer bracketOrder;

    /** Ngưỡng thu nhập tính thuế dưới (VND/tháng) */
    @Column(name = "from_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal fromAmount;

    /** Ngưỡng thu nhập tính thuế trên (VND/tháng), null nếu là bậc không giới hạn trần */
    @Column(name = "to_amount", precision = 15, scale = 2)
    private BigDecimal toAmount;

    /** Thuế suất áp dụng (ví dụ: 0.05 cho 5%, 0.10 cho 10%) */
    @Column(name = "tax_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal taxRate;

    /** Mô tả bậc thuế (ví dụ: "Đến 10 triệu đồng (5%)") */
    @Column(length = 200)
    private String description;
}
