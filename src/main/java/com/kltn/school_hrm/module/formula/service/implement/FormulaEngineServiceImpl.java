package com.kltn.school_hrm.module.formula.service.implement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.kltn.school_hrm.module.formula.dto.response.FormulaEvaluateResponse;
import com.kltn.school_hrm.module.formula.dto.response.FormulaValidateResponse;
import com.kltn.school_hrm.module.formula.engine.FormulaEvaluator;
import com.kltn.school_hrm.module.formula.engine.FormulaParser;
import com.kltn.school_hrm.module.formula.engine.expression.Expression;
import com.kltn.school_hrm.module.formula.service.FormulaEngineService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FormulaEngineServiceImpl implements FormulaEngineService {

    private final FormulaParser formulaParser;
    private final FormulaEvaluator formulaEvaluator;

    @Override
    public Expression parse(String formula) {
        return formulaParser.parse(formula);
    }

    @Override
    public FormulaValidateResponse validate(String formula) {
        Expression expression = formulaParser.parse(formula);
        Set<String> variables = new LinkedHashSet<>();
        expression.collectVariables(variables);
        return FormulaValidateResponse.builder()
                .valid(true)
                .message("Công thức hợp lệ")
                .usedVariables(new ArrayList<>(variables))
                .build();
    }

    @Override
    public FormulaEvaluateResponse evaluate(String formula, Map<String, BigDecimal> variables) {
        Expression expression = formulaParser.parse(formula);
        Set<String> used = new LinkedHashSet<>();
        expression.collectVariables(used);
        BigDecimal result = formulaEvaluator.evaluate(expression, variables);
        return FormulaEvaluateResponse.builder()
                .result(result)
                .expression(formula)
                .usedVariables(new ArrayList<>(used))
                .build();
    }
}
