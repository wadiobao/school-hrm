package com.kltn.school_hrm.module.scheduling.constraint;

import java.util.List;

import com.kltn.school_hrm.module.scheduling.entity.Timetable;

/**
 * Interface cho các quy tắc ràng buộc xếp lịch thời khóa biểu (Strategy Pattern).
 */
public interface ScheduleConstraint {
    List<ValidationError> validate(Timetable timetable);
}

