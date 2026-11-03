package com.kltn.school_hrm.module.scheduling.dto.response;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.kltn.school_hrm.shared.enums.Enums.TimeSlotSession;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotResponse {
    private Long id;
    private Integer periodNumber;
    private LocalTime startTime;
    private LocalTime endTime;
    private TimeSlotSession session;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
