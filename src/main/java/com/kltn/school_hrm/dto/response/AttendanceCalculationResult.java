package com.kltn.school_hrm.dto.response;

import com.kltn.school_hrm.enums.Enums.AttendanceStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceCalculationResult {

    /** Số phút đi trễ (đã trừ thời gian ân hạn graceMinutes). */
    @Builder.Default
    private Integer lateMinutes = 0;

    /** Số phút về sớm so với kết thúc ca. */
    @Builder.Default
    private Integer earlyLeaveMinutes = 0;

    /** Tổng số phút làm việc thực tế (đã trừ breakMinutes nếu có). */
    @Builder.Default
    private Integer workedMinutes = 0;

    /** Trạng thái chấm công được tính toán (PRESENT, LATE, EARLY_LEAVE, ABSENT). */
    private AttendanceStatus status;
}
