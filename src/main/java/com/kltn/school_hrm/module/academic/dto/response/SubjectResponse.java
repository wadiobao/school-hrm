package com.kltn.school_hrm.module.academic.dto.response;

import java.time.LocalDateTime;

import com.kltn.school_hrm.shared.enums.Enums.SubjectStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private SubjectStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

