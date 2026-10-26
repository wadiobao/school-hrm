package com.kltn.school_hrm.module.payroll.calculator;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.kltn.school_hrm.module.payroll.entity.TaxPolicy;

/**
 * Tính toán các khoản giảm trừ hợp lệ trước khi tính thuế TNCN:
 * - Bảo hiểm bắt buộc đã đóng
 * - Giảm trừ gia cảnh cho bản thân người nộp thuế
 * - Giảm trừ gia cảnh cho người phụ thuộc đủ điều kiện
 */
@Component
public class TaxDeductionCalculator {

    /**
     * Tính tổng các khoản được giảm trừ hợp lệ theo chính sách thuế hiện hành.
     *
     * @param insuranceDeduction Số tiền bảo hiểm người lao động đóng trong kỳ
     * @param dependentCount     Số người phụ thuộc đăng ký của nhân viên
     * @param policy             Chính sách thuế hiệu lực (chứa các định mức giảm trừ)
     * @return Tổng số tiền được giảm trừ hợp lệ
     */
    public BigDecimal calculateTotalDeduction(BigDecimal insuranceDeduction, int dependentCount, TaxPolicy policy) {
        BigDecimal insurance = insuranceDeduction != null ? insuranceDeduction : BigDecimal.ZERO;

        BigDecimal personal = (policy != null && policy.getPersonalDeduction() != null)
                ? policy.getPersonalDeduction()
                : new BigDecimal("15500000");

        BigDecimal dependentPerPerson = (policy != null && policy.getDependentDeduction() != null)
                ? policy.getDependentDeduction()
                : new BigDecimal("6200000");

        BigDecimal totalDependent = dependentCount > 0
                ? dependentPerPerson.multiply(BigDecimal.valueOf(dependentCount))
                : BigDecimal.ZERO;

        return insurance.add(personal).add(totalDependent);
    }
}
