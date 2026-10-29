package com.kltn.school_hrm.module.formula.engine.expression;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public final class UnaryMinusExpression implements Expression {

    private final Expression operand;

    public UnaryMinusExpression(Expression operand) {
        this.operand = operand;
    }

    @Override
    public BigDecimal evaluate(Map<String, BigDecimal> variables) {
        return operand.evaluate(variables).negate();
    }

    @Override
    public void collectVariables(Set<String> out) {
        operand.collectVariables(out);
    }

    @Override
    public int depth() {
        return 1 + operand.depth();
    }
}
