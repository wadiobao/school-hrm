package com.kltn.school_hrm.module.formula.engine;

import java.math.BigDecimal;
import java.util.Map;

import com.kltn.school_hrm.module.formula.engine.expression.Expression;

public interface FormulaEvaluator {

    BigDecimal evaluate(Expression expression, Map<String, BigDecimal> variables);
}
