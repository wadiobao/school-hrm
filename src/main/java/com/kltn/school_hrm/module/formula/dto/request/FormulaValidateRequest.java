package com.kltn.school_hrm.module.formula.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormulaValidateRequest {

    @NotBlank(message = "Biểu thức không được để trống")
    private String expression;
}
