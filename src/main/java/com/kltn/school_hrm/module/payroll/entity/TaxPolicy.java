package com.kltn.school_hrm.module.payroll.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.kltn.school_hrm.shared.entity.base.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Chính sách thuế TNCN và các mức giảm trừ gia cảnh, tỷ lệ bảo hiểm.
 * Hỗ trợ lưu trữ theo phiên bản và cấu hình động cho Quản trị viên (SUPER_ADMIN).
 */
@Entity
@Table(name = "tax_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TaxPolicy extends BaseEntity {

    /** Mã chính sách duy nhất, ví dụ: "TAX-VN-2026" */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /** Tên chính sách mô tả */
    @Column(nullable = false, length = 150)
    private String name;

    /** Phiên bản chính sách, ví dụ: "2026-v1" */
    @Column(nullable = false, length = 20)
    private String version;

    /** Ngày bắt đầu có hiệu lực */
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    /** Ngày hết hiệu lực (nếu null là vô thời hạn cho tới khi có policy mới) */
    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    /** Mức giảm trừ gia cảnh cho bản thân người nộp thuế (VND/tháng) */
    @Column(name = "personal_deduction", nullable = false, precision = 15, scale = 2)
    private BigDecimal personalDeduction;

    /** Mức giảm trừ cho mỗi người phụ thuộc đủ điều kiện (VND/tháng) */
    @Column(name = "dependent_deduction", nullable = false, precision = 15, scale = 2)
    private BigDecimal dependentDeduction;

    /** Tỷ lệ bảo hiểm bắt buộc người lao động đóng (mặc định 0.105 = 10.5%) */
    @Column(name = "insurance_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal insuranceRate;

    /** Trạng thái kích hoạt chính sách */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** Ghi chú căn cứ pháp lý hoặc quyết định áp dụng */
    @Column(columnDefinition = "TEXT")
    private String note;

    /** Danh sách các bậc thuế lũy tiến từng phần thuộc chính sách này */
    @OneToMany(mappedBy = "taxPolicy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("bracketOrder ASC")
    @Builder.Default
    private List<TaxBracket> brackets = new ArrayList<>();

    public void addBracket(TaxBracket bracket) {
        brackets.add(bracket);
        bracket.setTaxPolicy(this);
    }

    public void clearBrackets() {
        brackets.forEach(b -> b.setTaxPolicy(null));
        brackets.clear();
    }
}
