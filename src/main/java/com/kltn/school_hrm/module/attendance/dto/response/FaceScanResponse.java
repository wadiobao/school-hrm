package com.kltn.school_hrm.module.attendance.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kết quả trả về cho Camera AI Client (tương thích 100% với camera_client.py và Web Camera Simulator).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaceScanResponse {

    /** SUCCESS, COOLDOWN, hoặc ERROR */
    private String status;

    /** IN hoặc OUT */
    private String type;

    /** Chuỗi giờ quét HH:mm:ss để hiển thị trực tiếp lên OSD của OpenCV Camera */
    private String time;

    private String workDate;

    private Long employeeId;

    @JsonProperty("employee_code")
    private String employeeCode;

    private String employeeName;
    private String departmentName;
    private String positionName;
    private String deviceId;
    private Double confidence;

    private String attendanceStatus;
    private Integer lateMinutes;
    private Integer earlyLeaveMinutes;
    private Integer workedMinutes;

    private LocalDateTime eventTime;
    private String message;
}
