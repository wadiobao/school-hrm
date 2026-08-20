package com.kltn.school_hrm.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingNormResponse {
    private Long id;
    private String academicYear;
    private Long positionId;
    private Integer standardHours;
    private BigDecimal reductionPercentage;
}
