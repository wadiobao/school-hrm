package com.kltn.school_hrm.module.formula.engine;

import com.kltn.school_hrm.module.formula.engine.expression.Expression;

public interface FormulaParser {

    Expression parse(String formula);
}
