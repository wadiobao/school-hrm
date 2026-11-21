package com.kltn.school_hrm.module.attendance.dto.request;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO nhận sự kiện nhận diện khuôn mặt từ Camera AI (Python OpenCV/DeepFace hoặc Web Camera Simulator).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaceScanRequest {

    @NotBlank(message = "Mã nhân viên (employee_code) không được để trống")
    @JsonAlias({"employee_code", "employeeCode"})
    private String employeeCode;

    @JsonAlias({"device_id", "deviceId"})
    private String deviceId;

    private Double confidence;

    /**
     * Thời điểm giả lập chấm công (tùy chọn, phục vụ kiểm thử các kịch bản vào ca đúng giờ / đi trễ / tan ca).
     * Nếu null sẽ lấy thời gian thực LocalDateTime.now().
     */
    @JsonAlias({"simulated_time", "simulatedTime"})
    private LocalDateTime simulatedTime;

    /**
     * Ép kiểu sự kiện IN hoặc OUT (tùy chọn). Nếu để trống, hệ thống tự động đảo IN <-> OUT theo lịch sử ngày.
     */
    @JsonAlias({"force_type", "forceType"})
    private String forceType;

    /**
     * Bỏ qua thời gian chờ chống quét trùng (dùng khi test giả lập liên tiếp trên Web UI).
     */
    @JsonAlias({"bypass_cooldown", "bypassCooldown"})
    private Boolean bypassCooldown;
}
