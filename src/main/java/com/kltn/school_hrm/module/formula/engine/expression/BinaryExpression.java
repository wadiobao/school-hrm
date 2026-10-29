package com.kltn.school_hrm.module.formula.engine.expression;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;

import com.kltn.school_hrm.shared.exception.custom.BusinessException;

public final class BinaryExpression implements Expression {

    public static final int SCALE = 10;

    private final Expression left;
    private final char operator;
    private final Expression right;

    public BinaryExpression(Expression left, char operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public BigDecimal evaluate(Map<String, BigDecimal> variables) {
        BigDecimal l = left.evaluate(variables);
        BigDecimal r = right.evaluate(variables);
        return switch (operator) {
            case '+' -> l.add(r);
            case '-' -> l.subtract(r);
            case '*' -> l.multiply(r);
            case '/' -> {
                if (r.compareTo(BigDecimal.ZERO) == 0) {
                    throw new BusinessException("Công thức chia cho 0");
                }
                yield l.divide(r, SCALE, RoundingMode.HALF_UP);
            }
            default -> throw new BusinessException("Toán tử không được hỗ trợ: " + operator);
        };
    }

    @Override
    public void collectVariables(Set<String> out) {
        left.collectVariables(out);
        right.collectVariables(out);
    }

    @Override
    public int depth() {
        return 1 + Math.max(left.depth(), right.depth());
    }
}
