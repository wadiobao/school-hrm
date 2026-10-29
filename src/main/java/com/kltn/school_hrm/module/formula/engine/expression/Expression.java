package com.kltn.school_hrm.module.formula.engine.expression;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * Node AST của công thức sau khi parse.
 */
public interface Expression {

    BigDecimal evaluate(Map<String, BigDecimal> variables);

    void collectVariables(Set<String> out);

    int depth();
}
