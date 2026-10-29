package com.kltn.school_hrm.module.formula.dto.request;

import java.math.BigDecimal;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormulaEvaluateRequest {

    @NotBlank(message = "Biểu thức không được để trống")
    private String expression;

    private Map<String, BigDecimal> variables;
}
