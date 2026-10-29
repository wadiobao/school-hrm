package com.kltn.school_hrm.module.formula.engine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.kltn.school_hrm.module.formula.engine.expression.Expression;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;

@Component
public class BigDecimalFormulaEvaluator implements FormulaEvaluator {

    public static final int RESULT_SCALE = 2;

    @Override
    public BigDecimal evaluate(Expression expression, Map<String, BigDecimal> variables) {
        if (expression == null) {
            throw new BusinessException("Biểu thức chưa được phân tích");
        }
        Map<String, BigDecimal> safeVars = variables == null ? Map.of() : variables;
        BigDecimal raw = expression.evaluate(safeVars);
        return raw.setScale(RESULT_SCALE, RoundingMode.HALF_UP);
    }
}
