package com.kltn.school_hrm.module.formula.engine.expression;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import com.kltn.school_hrm.shared.exception.custom.BusinessException;

public final class VariableExpression implements Expression {

    private final String name;

    public VariableExpression(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public BigDecimal evaluate(Map<String, BigDecimal> variables) {
        BigDecimal value = variables.get(name);
        if (value == null) {
            throw new BusinessException("Thiếu giá trị cho biến: " + name);
        }
        return value;
    }

    @Override
    public void collectVariables(Set<String> out) {
        out.add(name);
    }

    @Override
    public int depth() {
        return 1;
    }
}
