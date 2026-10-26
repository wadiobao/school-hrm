package com.kltn.school_hrm.module.payroll.tax;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tổng hợp kết quả phân tích và tính toán thuế TNCN theo quy định chuẩn:
 * Taxable Income -> Total Deductions -> Taxable Base -> Progressive PIT.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxCalculationResult {

    /** Tổng thu nhập chịu thuế (Taxable Income) */
    private BigDecimal taxableIncome;

    /** Khoản bảo hiểm bắt buộc được giảm trừ */
    private BigDecimal insuranceDeduction;

    /** Giảm trừ gia cảnh bản thân */
    private BigDecimal personalDeduction;

    /** Số người phụ thuộc */
    private int dependentCount;

    /** Tổng giảm trừ người phụ thuộc = dependentCount * dependentDeduction */
    private BigDecimal dependentDeduction;

    /** Tổng các khoản giảm trừ hợp lệ = BH + Bản thân + Người phụ thuộc */
    private BigDecimal totalDeductions;

    /** Thu nhập tính thuế (Taxable Base) = max(0, Taxable Income - Total Deductions) */
    private BigDecimal taxableBase;

    /** Số tiền thuế TNCN phải nộp (PIT Amount) sau khi lũy tiến từng phần */
    private BigDecimal pitAmount;

    /** Phiên bản chính sách thuế áp dụng tại thời điểm tính */
    private String policyVersion;

    /** Chi tiết tính toán trên từng bậc lũy tiến */
    @Builder.Default
    private List<TaxBracketDetail> bracketDetails = new ArrayList<>();
}
