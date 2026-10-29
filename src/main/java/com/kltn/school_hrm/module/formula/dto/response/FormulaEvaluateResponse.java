package com.kltn.school_hrm.module.formula.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormulaEvaluateResponse {
    private BigDecimal result;
    private String expression;
    private List<String> usedVariables;
}
