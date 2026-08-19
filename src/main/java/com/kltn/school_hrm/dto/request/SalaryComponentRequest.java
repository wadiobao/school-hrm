package com.kltn.school_hrm.dto.request;

import java.math.BigDecimal;

import com.kltn.school_hrm.enums.Enums.ComponentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryComponentRequest {
    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Component type is required")
    private ComponentType type;

    private Boolean isTaxable;
    private String formulaExpression;
}
