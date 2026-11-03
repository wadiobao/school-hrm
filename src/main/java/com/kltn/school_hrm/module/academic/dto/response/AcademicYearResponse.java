package com.kltn.school_hrm.module.academic.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.kltn.school_hrm.shared.enums.Enums.AcademicStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicYearResponse {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private AcademicStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

