package com.kltn.school_hrm.dto.response;

import com.kltn.school_hrm.enums.Enums.ComponentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryComponentResponse {
    private Long id;
    private String code;
    private String name;
    private ComponentType type;
    private Boolean isTaxable;
    private String formulaExpression;
}
