package com.kltn.school_hrm.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndAssignmentRequest {

    @NotNull(message = "Effective to date is required")
    private LocalDate effectiveTo;
}
