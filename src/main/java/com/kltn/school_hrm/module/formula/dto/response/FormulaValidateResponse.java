package com.kltn.school_hrm.module.formula.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormulaValidateResponse {
    private boolean valid;
    private String message;
    private List<String> usedVariables;
}
