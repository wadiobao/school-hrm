package com.kltn.school_hrm.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingNormRequest {
    @NotBlank(message = "Academic year is required")
    private String academicYear;

    @NotNull(message = "Position ID is required")
    private Long positionId;

    @NotNull @Positive
    private Integer standardHours;

    private BigDecimal reductionPercentage;
}
