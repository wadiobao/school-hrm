package com.kltn.school_hrm.module.scheduling.dto.request;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cấu hình đầu vào cho thuật toán tự động phân bổ giáo viên và xếp thời khóa biểu toàn trường theo học kỳ.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoScheduleSettingRequest {

    @NotNull(message = "Học kỳ không được để trống")
    private Long semesterId;

    /**
     * Danh sách các ngày học trong tuần (1 = Thứ 2, ..., 6 = Thứ 7).
     * Mặc định: [1, 2, 3, 4, 5, 6]
     */
    private List<Integer> activeDays;

    /**
     * Chế độ buổi học: MORNING (chỉ sáng), AFTERNOON (chỉ chiều), BOTH (cả sáng và chiều).
     * Mặc định: MORNING
     */
    private String sessionMode;

    /**
     * Số tiết học tối đa mỗi ngày cho mỗi lớp (mặc định: 5 tiết/buổi hoặc 8 tiết/ngày).
     */
    private Integer maxPeriodsPerDay;

    /**
     * Số tiết tối đa của cùng 1 môn học trong 1 ngày cho 1 lớp (mặc định: 2 tiết).
     */
    private Integer maxPeriodsPerSubjectPerDay;

    /**
     * Tổng số tiết học mục tiêu mỗi tuần của một lớp (mặc định: 23 tiết/tuần).
     * Dùng để quy đổi tỉ lệ trọng số các môn học sang số tiết thực tế theo phương pháp Hamilton (Largest Remainder).
     */
    private Integer targetWeeklyPeriods;

    /**
     * Tỉ lệ / Trọng số của từng môn học (Key: Mã môn học như MATH, LIT, ENG... -> Value: tỉ lệ hoặc số tiết).
     */
    private Map<String, Double> subjectRatios;

    /**
     * Có tự động tính toán lại phân công giáo viên (TeachingAssignment) theo công thức cân bằng tải hay không.
     */
    private Boolean autoAssignTeachers;

    /**
     * Có ghi đè / tạo mới lại thời khóa biểu cho các lớp đã có TKB trong học kỳ này hay không.
     */
    private Boolean overwriteExisting;

    /**
     * Có tự động kiểm tra ràng buộc và chuyển trạng thái sang PUBLISHED sau khi xếp lịch hoàn tất hay không.
     */
    private Boolean autoPublish;
}
