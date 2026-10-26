package com.kltn.school_hrm.module.payroll.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.kltn.school_hrm.module.payroll.entity.TaxPolicy;

/**
 * Tính các khoản bảo hiểm bắt buộc của người lao động (BHXH, BHYT, BHTN)
 * làm căn cứ để trừ khỏi thu nhập chịu thuế theo quy định pháp luật.
 */
@Component
public class InsuranceCalculator {

    /**
     * Tính số tiền bảo hiểm được khấu trừ theo mức lương đóng bảo hiểm và tỷ lệ quy định trong chính sách.
     *
     * @param insuranceSalary Mức lương làm căn cứ đóng bảo hiểm (lương hợp đồng)
     * @param policy          Chính sách thuế chứa tỷ lệ bảo hiểm đang áp dụng
     * @return Số tiền bảo hiểm được giảm trừ (làm tròn nguyên VND)
     */
    public BigDecimal calculate(BigDecimal insuranceSalary, TaxPolicy policy) {
        if (insuranceSalary == null || insuranceSalary.signum() <= 0 || policy == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal rate = policy.getInsuranceRate() != null ? policy.getInsuranceRate() : new BigDecimal("0.105");
        return insuranceSalary.multiply(rate).setScale(0, RoundingMode.HALF_UP);
    }
}
