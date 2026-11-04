package com.kltn.school_hrm.module.scheduling.dto.request;

import java.time.LocalTime;

import com.kltn.school_hrm.shared.enums.Enums.TimeSlotSession;

import jakarta.validation.constraints.Max;
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
public class TimeSlotRequest {

    @NotNull(message = "Số thứ tự tiết không được để trống")
    @Min(value = 1, message = "Tiết học tối thiểu là 1")
    @Max(value = 10, message = "Tiết học tối đa là 10")
    private Integer periodNumber;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;

    @NotNull(message = "Buổi học không được để trống")
    private TimeSlotSession session;
}

