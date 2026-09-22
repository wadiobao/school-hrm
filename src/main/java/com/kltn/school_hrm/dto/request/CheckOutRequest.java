package com.kltn.school_hrm.dto.request;

import java.time.LocalDate;
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
public class CheckOutRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    /** Ngày làm việc cần checkout (nếu null sẽ lấy ngày hôm nay). */
    private LocalDate workDate;

    /** Thời gian check-out (nếu null sẽ lấy thời điểm hiện tại). */
    private LocalDateTime checkOutTime;

    /** Mã thiết bị chấm công (tùy chọn). */
    private String deviceId;
}
