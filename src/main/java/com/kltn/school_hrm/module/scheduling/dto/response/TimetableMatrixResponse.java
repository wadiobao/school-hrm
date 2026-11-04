package com.kltn.school_hrm.module.scheduling.dto.response;

import java.util.List;
import java.util.Map;

import com.kltn.school_hrm.shared.enums.Enums.TimetableStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO trả về dữ liệu ma trận thời khóa biểu tuần (Thứ 2 đến Thứ 6/Thứ 7)
 * phục vụ trực tiếp cho màn hình Timetable Editor dạng lưới (Grid View).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableMatrixResponse {
    private Long timetableId;
    private Long schoolClassId;
    private String schoolClassName;
    private Long semesterId;
    private String semesterName;
    private Integer version;
    private TimetableStatus status;
    private List<TimeSlotResponse> timeSlots;
    // Map theo key "dayOfWeek:timeSlotId" -> TimetableEntryResponse
    private Map<String, TimetableEntryResponse> gridEntries;
}

