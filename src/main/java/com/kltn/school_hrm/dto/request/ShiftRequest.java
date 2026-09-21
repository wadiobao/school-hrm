package com.kltn.school_hrm.dto.request;

import java.time.LocalTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO dùng cho cả tạo mới và cập nhật ca làm việc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftRequest {

    @NotBlank(message = "Tên ca làm việc không được để trống")
    private String name;

    @NotBlank(message = "Mã ca làm việc không được để trống")
    private String code;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;

    /** Ca có kéo qua ngày hôm sau không (VD: ca đêm). Mặc định false. */
    @Builder.Default
    private Boolean overNight = false;

    /** Số phút nghỉ giữa ca. Mặc định 0. */
    @Min(value = 0, message = "Số phút nghỉ không được âm")
    @Builder.Default
    private Integer breakMinutes = 0;

    /** Số phút cho phép đến trễ / về sớm trước khi bị đánh là đi muộn. Mặc định 0. */
    @Min(value = 0, message = "Số phút ân hạn không được âm")
    @Builder.Default
    private Integer graceMinutes = 0;

    @NotNull(message = "Trạng thái hoạt động không được để trống")
    private Boolean isActive;
}
