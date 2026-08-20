package com.kltn.school_hrm.dto.request;

import java.time.LocalDate;

import com.kltn.school_hrm.enums.Enums.TeachingLogType;
import com.kltn.school_hrm.enums.Enums.RequestStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingLogRequest {
    @NotNull(message = "Assignment ID is required")
    private Long assignmentId;

    @NotNull(message = "Actual teacher ID is required")
    private Long actualTeacherId;

    @NotNull(message = "Teaching date is required")
    private LocalDate teachingDate;

    @NotNull
    @Min(1)
    private Integer periodsTaught;

    private TeachingLogType type;

    private RequestStatus status;
}
