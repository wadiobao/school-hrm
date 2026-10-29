package com.kltn.school_hrm.module.formula.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormulaVariableResponse {
    private String code;
    private String name;
    private String dataType;
    private String sampleValue;
    private String description;
}
