package com.kltn.school_hrm.module.academic.dto.response;

import java.time.LocalDateTime;

import com.kltn.school_hrm.shared.enums.Enums.ClassroomStatus;
import com.kltn.school_hrm.shared.enums.Enums.ClassroomType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomResponse {
    private Long id;
    private String code;
    private String name;
    private Integer capacity;
    private ClassroomType roomType;
    private ClassroomStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

