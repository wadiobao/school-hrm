package com.kltn.school_hrm.module.formula.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.kltn.school_hrm.shared.enums.Enums.FormulaStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormulaDefinitionResponse {
    private Long id;
    private String code;
    private String name;
    private String expression;
    private String version;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private FormulaStatus status;
    private String note;
    private List<String> usedVariables;
    private LocalDateTime updatedAt;
}
