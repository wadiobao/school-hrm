package com.kltn.school_hrm.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    /** Thời gian check-in (nếu null sẽ lấy thời điểm hiện tại). */
    private LocalDateTime checkInTime;

    /** Mã thiết bị chấm công (tùy chọn). */
    private String deviceId;
}
