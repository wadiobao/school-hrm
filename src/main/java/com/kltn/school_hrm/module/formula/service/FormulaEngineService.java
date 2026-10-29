package com.kltn.school_hrm.module.formula.service;

import java.math.BigDecimal;
import java.util.Map;

import com.kltn.school_hrm.module.formula.dto.response.FormulaEvaluateResponse;
import com.kltn.school_hrm.module.formula.dto.response.FormulaValidateResponse;
import com.kltn.school_hrm.module.formula.engine.expression.Expression;

public interface FormulaEngineService {

    Expression parse(String formula);

    FormulaValidateResponse validate(String formula);

    FormulaEvaluateResponse evaluate(String formula, Map<String, BigDecimal> variables);
}
