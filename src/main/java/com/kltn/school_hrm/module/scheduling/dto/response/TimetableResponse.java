package com.kltn.school_hrm.module.scheduling.dto.response;

import java.time.LocalDateTime;

import com.kltn.school_hrm.shared.enums.Enums.TimetableStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableResponse {
    private Long id;
    private Long schoolClassId;
    private String schoolClassName;
    private Long semesterId;
    private String semesterName;
    private Integer version;
    private TimetableStatus status;
    private Long createdByUserId;
    private String createdByUserName;
    private LocalDateTime publishedAt;
    private String validationNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

