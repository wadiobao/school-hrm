package com.kltn.school_hrm.module.payroll.calculator;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.kltn.school_hrm.module.employee.entity.Contract;
import com.kltn.school_hrm.module.payroll.entity.TaxPolicy;

/**
 * Phân loại và tổng hợp các khoản thu nhập chịu thuế (Taxable Income) trong kỳ lương.
 * Đảm bảo phân biệt rõ ràng giữa tổng thu nhập (Gross) và các khoản thuộc diện chịu thuế theo chính sách.
 */
@Component
public class TaxableIncomeCalculator {

    /**
     * Xác định tổng thu nhập chịu thuế từ lương theo ngày công và các khoản phụ cấp hợp đồng.
     *
     * @param baseSalary Lương thực tế được trả theo ngày công làm việc
     * @param contract   Hợp đồng lao động chứa các phụ cấp (nhà ở, đi lại, di dời)
     * @param policy     Chính sách thuế áp dụng
     * @return Tổng thu nhập chịu thuế TNCN
     */
    public BigDecimal calculate(BigDecimal baseSalary, Contract contract, TaxPolicy policy) {
        BigDecimal total = baseSalary != null ? baseSalary : BigDecimal.ZERO;

        if (contract != null) {
            if (contract.getHousingAllowance() != null) {
                total = total.add(contract.getHousingAllowance());
            }
            if (contract.getFlightAllowance() != null) {
                total = total.add(contract.getFlightAllowance());
            }
            if (contract.getRelocationAllowance() != null) {
                total = total.add(contract.getRelocationAllowance());
            }
        }

        return total;
    }

    /**
     * Phương thức tiện ích tính toán trực tiếp từ các khoản số tiền cụ thể.
     */
    public BigDecimal calculate(BigDecimal baseSalary, BigDecimal housingAllowance,
            BigDecimal flightAllowance, BigDecimal relocationAllowance, TaxPolicy policy) {
        BigDecimal total = baseSalary != null ? baseSalary : BigDecimal.ZERO;
        if (housingAllowance != null) total = total.add(housingAllowance);
        if (flightAllowance != null) total = total.add(flightAllowance);
        if (relocationAllowance != null) total = total.add(relocationAllowance);
        return total;
    }
}
