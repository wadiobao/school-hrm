package com.kltn.school_hrm.module.formula.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.kltn.school_hrm.module.formula.engine.expression.Expression;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;

class RecursiveDescentFormulaParserTest {

    private RecursiveDescentFormulaParser parser;
    private BigDecimalFormulaEvaluator evaluator;

    @BeforeEach
    void setUp() {
        parser = new RecursiveDescentFormulaParser(new FormulaVariableRegistry());
        evaluator = new BigDecimalFormulaEvaluator();
    }

    @Test
    @DisplayName("Ưu tiên nhân trước cộng: baseSalary + allowance * 2")
    void shouldRespectOperatorPrecedence() {
        Expression expression = parser.parse("baseSalary + allowance * 2");
        BigDecimal result = evaluator.evaluate(expression, Map.of(
                "baseSalary", new BigDecimal("12000000"),
                "allowance", new BigDecimal("500000")));
        assertEquals(new BigDecimal("13000000.00"), result);
    }

    @Test
    @DisplayName("Tính overtime theo công thức trong thiết kế")
    void shouldEvaluateOvertimePay() {
        Expression expression = parser.parse(
                "baseSalary / standardWorkHours * overtimeHours * overtimeRate");
        BigDecimal result = evaluator.evaluate(expression, Map.of(
                "baseSalary", new BigDecimal("12000000"),
                "standardWorkHours", new BigDecimal("176"),
                "overtimeHours", new BigDecimal("10"),
                "overtimeRate", new BigDecimal("1.5")));
        assertEquals(new BigDecimal("1022727.27"), result);
    }

    @Test
    @DisplayName("Từ chối biến chưa đăng ký")
    void shouldRejectUnknownVariable() {
        assertThrows(BusinessException.class, () -> parser.parse("baseSalary + unknownField"));
    }

    @Test
    @DisplayName("Từ chối chia cho 0 khi evaluate")
    void shouldRejectDivideByZero() {
        Expression expression = parser.parse("baseSalary / standardWorkHours");
        assertThrows(BusinessException.class,
                () -> evaluator.evaluate(expression, Map.of(
                        "baseSalary", new BigDecimal("12000000"),
                        "standardWorkHours", BigDecimal.ZERO)));
    }
}
