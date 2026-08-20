package com.kltn.school_hrm.dto.response;

import java.time.LocalDate;

import com.kltn.school_hrm.enums.Enums.TeachingLogType;
import com.kltn.school_hrm.enums.Enums.RequestStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeachingLogResponse {
    private Long id;
    private Long assignmentId;
    private Long actualTeacherId;
    private LocalDate teachingDate;
    private Integer periodsTaught;
    private TeachingLogType type;
    private RequestStatus status;
}
