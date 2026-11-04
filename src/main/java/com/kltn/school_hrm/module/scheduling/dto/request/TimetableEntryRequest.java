package com.kltn.school_hrm.module.scheduling.dto.request;

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
public class TimetableEntryRequest {

    @NotNull(message = "ID thời khóa biểu không được để trống")
    private Long timetableId;

    @NotNull(message = "ID phân công giảng dạy không được để trống")
    private Long teachingAssignmentId;

    private Long substituteTeacherId; // Giáo viên dạy thay (tùy chọn)

    @NotNull(message = "Thứ trong tuần không được để trống")
    @Min(value = 1, message = "Thứ trong tuần tối thiểu là 1 (Thứ 2)")
    @Max(value = 7, message = "Thứ trong tuần tối đa là 7 (Chủ nhật)")
    private Integer dayOfWeek; // 1 = Monday, 2 = Tuesday, ..., 7 = Sunday

    @NotNull(message = "ID tiết học không được để trống")
    private Long timeSlotId;

    private Long classroomId; // ID phòng học (tùy chọn)
}

