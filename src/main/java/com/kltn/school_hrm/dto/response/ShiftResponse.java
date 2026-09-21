package com.kltn.school_hrm.dto.response;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO trả về thông tin ca làm việc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftResponse {

    private Long id;
    private String name;
    private String code;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean overNight;
    private Integer breakMinutes;
    private Integer graceMinutes;
    private Boolean isActive;
}
