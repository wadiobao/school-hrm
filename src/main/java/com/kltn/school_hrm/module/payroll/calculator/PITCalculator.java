package com.kltn.school_hrm.module.payroll.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.kltn.school_hrm.module.payroll.entity.TaxBracket;
import com.kltn.school_hrm.module.payroll.entity.TaxPolicy;
import com.kltn.school_hrm.module.payroll.tax.TaxBracketDetail;
import com.kltn.school_hrm.module.payroll.tax.TaxCalculationResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Bộ tính thuế thu nhập cá nhân (Personal Income Tax - PIT Calculator).
 * Áp dụng biểu thuế lũy tiến từng phần theo quy định của Luật Thuế 2026.
 * Đảm bảo độc lập với các chính sách khác và cho phép kiểm tra chi tiết theo từng bậc thuế.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PITCalculator {

    private final TaxDeductionCalculator taxDeductionCalculator;

    /**
     * Tính toán thuế TNCN hoàn chỉnh từ thu nhập chịu thuế, số người phụ thuộc và chính sách thuế.
     *
     * @param taxableIncome      Tổng thu nhập chịu thuế trong kỳ
     * @param insuranceDeduction Khoản tiền bảo hiểm đã trừ
     * @param dependentCount     Số người phụ thuộc đủ điều kiện giảm trừ
     * @param policy             Chính sách thuế hiệu lực (chứa biểu thuế và các định mức giảm trừ)
     * @return TaxCalculationResult chứa số thuế TNCN và bảng kê chi tiết từng bậc
     */
    public TaxCalculationResult calculate(BigDecimal taxableIncome, BigDecimal insuranceDeduction,
            int dependentCount, TaxPolicy policy) {

        BigDecimal income = taxableIncome != null ? taxableIncome : BigDecimal.ZERO;
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

        BigDecimal totalDeductions = insurance.add(personal).add(totalDependent);
        BigDecimal taxableBase = income.subtract(totalDeductions).max(BigDecimal.ZERO);

        List<TaxBracket> brackets = getSortedBrackets(policy);
        List<TaxBracketDetail> details = new ArrayList<>();
        BigDecimal totalPit = BigDecimal.ZERO;

        if (taxableBase.compareTo(BigDecimal.ZERO) > 0 && !brackets.isEmpty()) {
            for (TaxBracket bracket : brackets) {
                BigDecimal from = bracket.getFromAmount();
                BigDecimal to = bracket.getToAmount();
                BigDecimal rate = bracket.getTaxRate();

                if (taxableBase.compareTo(from) <= 0) {
                    continue;
                }

                BigDecimal portion;
                if (to != null) {
                    BigDecimal maxInBracket = to.subtract(from);
                    BigDecimal excess = taxableBase.subtract(from);
                    portion = excess.min(maxInBracket);
                } else {
                    portion = taxableBase.subtract(from);
                }

                BigDecimal taxInBracket = portion.multiply(rate).setScale(0, RoundingMode.HALF_UP);
                totalPit = totalPit.add(taxInBracket);

                details.add(TaxBracketDetail.builder()
                        .bracketOrder(bracket.getBracketOrder())
                        .fromAmount(from)
                        .toAmount(to)
                        .taxRate(rate)
                        .taxableAmountInBracket(portion)
                        .taxInBracket(taxInBracket)
                        .build());
            }
        }

        String version = (policy != null && policy.getVersion() != null) ? policy.getVersion() : "2026-v1";

        return TaxCalculationResult.builder()
                .taxableIncome(income)
                .insuranceDeduction(insurance)
                .personalDeduction(personal)
                .dependentCount(dependentCount)
                .dependentDeduction(totalDependent)
                .totalDeductions(totalDeductions)
                .taxableBase(taxableBase)
                .pitAmount(totalPit)
                .policyVersion(version)
                .bracketDetails(details)
                .build();
    }

    /**
     * Lấy danh sách bậc thuế đã sắp xếp thứ tự hoặc fallback về biểu thuế 5 bậc chuẩn 2026.
     */
    private List<TaxBracket> getSortedBrackets(TaxPolicy policy) {
        if (policy != null && policy.getBrackets() != null && !policy.getBrackets().isEmpty()) {
            return policy.getBrackets().stream()
                    .sorted(Comparator.comparingInt(TaxBracket::getBracketOrder))
                    .toList();
        }
        return createDefault2026Brackets();
    }

    /**
     * Khởi tạo biểu thuế lũy tiến 5 bậc chuẩn năm 2026 làm cơ chế bảo vệ nếu chính sách chưa cấu hình bậc thuế.
     */
    public static List<TaxBracket> createDefault2026Brackets() {
        return List.of(
                TaxBracket.builder().bracketOrder(1).fromAmount(BigDecimal.ZERO).toAmount(new BigDecimal("10000000"))
                        .taxRate(new BigDecimal("0.05")).description("Đến 10 triệu đồng (5%)").build(),
                TaxBracket.builder().bracketOrder(2).fromAmount(new BigDecimal("10000000")).toAmount(new BigDecimal("30000000"))
                        .taxRate(new BigDecimal("0.10")).description("Trên 10 - 30 triệu đồng (10%)").build(),
                TaxBracket.builder().bracketOrder(3).fromAmount(new BigDecimal("30000000")).toAmount(new BigDecimal("60000000"))
                        .taxRate(new BigDecimal("0.20")).description("Trên 30 - 60 triệu đồng (20%)").build(),
                TaxBracket.builder().bracketOrder(4).fromAmount(new BigDecimal("60000000")).toAmount(new BigDecimal("100000000"))
                        .taxRate(new BigDecimal("0.30")).description("Trên 60 - 100 triệu đồng (30%)").build(),
                TaxBracket.builder().bracketOrder(5).fromAmount(new BigDecimal("100000000")).toAmount(null)
                        .taxRate(new BigDecimal("0.35")).description("Trên 100 triệu đồng (35%)").build()
        );
    }
}
