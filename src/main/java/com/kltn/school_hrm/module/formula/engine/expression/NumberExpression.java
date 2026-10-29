package com.kltn.school_hrm.module.formula.engine.expression;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public final class NumberExpression implements Expression {

    private final BigDecimal value;

    public NumberExpression(BigDecimal value) {
        this.value = value;
    }

    @Override
    public BigDecimal evaluate(Map<String, BigDecimal> variables) {
        return value;
    }

    @Override
    public void collectVariables(Set<String> out) {
        // no-op
    }

    @Override
    public int depth() {
        return 1;
    }
}
