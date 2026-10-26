package com.kltn.school_hrm.module.payroll.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.kltn.school_hrm.module.payroll.entity.TaxBracket;
import com.kltn.school_hrm.module.payroll.entity.TaxPolicy;
import com.kltn.school_hrm.module.payroll.tax.TaxCalculationResult;

/**
 * Unit Test kiểm thử tính thuế TNCN (PIT Calculator) theo quy định Luật Thuế năm 2026.
 *
 * <p>Kiểm tra:
 * <ul>
 *   <li>Thu nhập tính thuế theo ví dụ trong req.md (Gross 30tr -> PIT 635.000 VND)</li>
 *   <li>Thu nhập dưới mức giảm trừ gia cảnh (PIT = 0)</li>
 *   <li>Tính giảm trừ cho người phụ thuộc (6.2tr/người)</li>
 *   <li>Tùy chỉnh động biểu thuế và mức giảm trừ theo chính sách do SUPER_ADMIN cấu hình</li>
 * </ul>
 * </p>
 */
class PITCalculatorTest {

    private PITCalculator pitCalculator;
    private TaxPolicy policy2026;

    @BeforeEach
    void setUp() {
        TaxDeductionCalculator taxDeductionCalculator = new TaxDeductionCalculator();
        pitCalculator = new PITCalculator(taxDeductionCalculator);

        // Khởi tạo TaxPolicy chuẩn 2026
        policy2026 = TaxPolicy.builder()
                .code("TAX-VN-2026")
                .name("Chính sách thuế TNCN năm 2026")
                .version("2026-v1")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .personalDeduction(new BigDecimal("15500000"))
                .dependentDeduction(new BigDecimal("6200000"))
                .insuranceRate(new BigDecimal("0.105"))
                .isActive(true)
                .brackets(new ArrayList<>(PITCalculator.createDefault2026Brackets()))
                .build();
    }

    @Test
    @DisplayName("Test ví dụ trong req.md: Thu nhập 30tr, BH 3.15tr, Bản thân 15.5tr, 0 người phụ thuộc -> Thuế = 635.000 VNĐ")
    void testCalculate_ExampleFromRequirements() {
        BigDecimal taxableIncome = new BigDecimal("30000000");
        BigDecimal insurance = new BigDecimal("3150000");
        int dependentCount = 0;

        TaxCalculationResult result = pitCalculator.calculate(taxableIncome, insurance, dependentCount, policy2026);

        assertNotNull(result);
        assertEquals(new BigDecimal("30000000"), result.getTaxableIncome());
        assertEquals(new BigDecimal("3150000"), result.getInsuranceDeduction());
        assertEquals(new BigDecimal("15500000"), result.getPersonalDeduction());
        assertEquals(BigDecimal.ZERO, result.getDependentDeduction());
        // Tổng giảm trừ = 3.15tr + 15.5tr = 18.650.000 VNĐ
        assertEquals(new BigDecimal("18650000"), result.getTotalDeductions());
        // Thu nhập tính thuế = 30tr - 18.65tr = 11.350.000 VNĐ
        assertEquals(new BigDecimal("11350000"), result.getTaxableBase());

        // Thuế = Bậc 1 (10tr * 5% = 500k) + Bậc 2 (1.35tr * 10% = 135k) = 635.000 VNĐ
        assertEquals(new BigDecimal("635000"), result.getPitAmount());
        assertEquals("2026-v1", result.getPolicyVersion());
    }

    @Test
    @DisplayName("Test thu nhập dưới ngưỡng giảm trừ gia cảnh -> Thuế = 0 VNĐ")
    void testCalculate_IncomeBelowDeductions_ZeroTax() {
        BigDecimal taxableIncome = new BigDecimal("15000000");
        BigDecimal insurance = new BigDecimal("1575000");
        int dependentCount = 0;

        TaxCalculationResult result = pitCalculator.calculate(taxableIncome, insurance, dependentCount, policy2026);

        assertEquals(new BigDecimal("17075000"), result.getTotalDeductions());
        assertEquals(BigDecimal.ZERO, result.getTaxableBase());
        assertEquals(BigDecimal.ZERO, result.getPitAmount());
    }

    @Test
    @DisplayName("Test có người phụ thuộc: 2 người phụ thuộc x 6.2tr = 12.4tr giảm trừ thêm")
    void testCalculate_WithDependents() {
        BigDecimal taxableIncome = new BigDecimal("30000000");
        BigDecimal insurance = new BigDecimal("3150000");
        int dependentCount = 2; // 2 người phụ thuộc

        TaxCalculationResult result = pitCalculator.calculate(taxableIncome, insurance, dependentCount, policy2026);

        // Giảm trừ người phụ thuộc = 2 * 6.2tr = 12.400.000
        assertEquals(new BigDecimal("12400000"), result.getDependentDeduction());
        // Tổng giảm trừ = 3.15tr + 15.5tr + 12.4tr = 31.050.000 VNĐ
        assertEquals(new BigDecimal("31050000"), result.getTotalDeductions());
        // 30tr < 31.05tr -> Thu nhập tính thuế = 0 -> Thuế = 0
        assertEquals(BigDecimal.ZERO, result.getTaxableBase());
        assertEquals(BigDecimal.ZERO, result.getPitAmount());
    }

    @Test
    @DisplayName("Test có 1 người phụ thuộc: Giảm trừ 6.2tr -> Thu nhập tính thuế 5.15tr -> Thuế = 257.500 VNĐ")
    void testCalculate_WithOneDependent() {
        BigDecimal taxableIncome = new BigDecimal("30000000");
        BigDecimal insurance = new BigDecimal("3150000");
        int dependentCount = 1;

        TaxCalculationResult result = pitCalculator.calculate(taxableIncome, insurance, dependentCount, policy2026);

        // Tổng giảm trừ = 3.15tr + 15.5tr + 6.2tr = 24.850.000 VNĐ
        assertEquals(new BigDecimal("24850000"), result.getTotalDeductions());
        // Thu nhập tính thuế = 30tr - 24.85tr = 5.150.000 VNĐ
        assertEquals(new BigDecimal("5150000"), result.getTaxableBase());
        // Thuế = Bậc 1 (5.15tr * 5%) = 257.500 VNĐ
        assertEquals(new BigDecimal("257500"), result.getPitAmount());
    }

    @Test
    @DisplayName("Test chính sách thuế được SUPER_ADMIN chỉnh sửa động: Đổi giảm trừ bản thân lên 18tr, đổi thuế suất")
    void testCalculate_CustomTaxPolicy() {
        TaxPolicy customPolicy = TaxPolicy.builder()
                .code("TAX-CUSTOM")
                .name("Chính sách tùy chỉnh")
                .version("custom-v1")
                .personalDeduction(new BigDecimal("18000000"))
                .dependentDeduction(new BigDecimal("7000000"))
                .insuranceRate(new BigDecimal("0.105"))
                .isActive(true)
                .brackets(List.of(
                        TaxBracket.builder().bracketOrder(1).fromAmount(BigDecimal.ZERO).toAmount(new BigDecimal("10000000"))
                                .taxRate(new BigDecimal("0.08")).description("Bậc 1: 8%").build(),
                        TaxBracket.builder().bracketOrder(2).fromAmount(new BigDecimal("10000000")).toAmount(null)
                                .taxRate(new BigDecimal("0.15")).description("Bậc 2: 15%").build()
                ))
                .build();

        BigDecimal taxableIncome = new BigDecimal("30000000");
        BigDecimal insurance = new BigDecimal("2000000");
        int dependentCount = 0;

        TaxCalculationResult result = pitCalculator.calculate(taxableIncome, insurance, dependentCount, customPolicy);

        // Tổng giảm trừ = 2tr + 18tr = 20tr
        assertEquals(new BigDecimal("20000000"), result.getTotalDeductions());
        // Thu nhập tính thuế = 30tr - 20tr = 10tr
        assertEquals(new BigDecimal("10000000"), result.getTaxableBase());
        // Thuế = 10tr * 8% = 800.000 VNĐ
        assertEquals(new BigDecimal("800000"), result.getPitAmount());
        assertEquals("custom-v1", result.getPolicyVersion());
    }
}
