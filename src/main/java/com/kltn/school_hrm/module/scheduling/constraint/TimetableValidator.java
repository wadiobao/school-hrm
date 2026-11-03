package com.kltn.school_hrm.module.scheduling.constraint;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.kltn.school_hrm.module.scheduling.entity.Timetable;

import lombok.RequiredArgsConstructor;

/**
 * Động cơ điều phối và tổng hợp kết quả kiểm tra ràng buộc thời khóa biểu.
 */
@Component
@RequiredArgsConstructor
public class TimetableValidator {

    private final List<ScheduleConstraint> constraints;

    /**
     * Chạy toàn bộ các quy tắc ràng buộc (Conflict, Period count, Workload...).
     * @param timetable Bản ghi thời khóa biểu cần kiểm tra
     * @return ValidationResult chứa danh sách lỗi (nếu có)
     */
    public ValidationResult validate(Timetable timetable) {
        List<ValidationError> allErrors = new ArrayList<>();

        for (ScheduleConstraint constraint : constraints) {
            List<ValidationError> errors = constraint.validate(timetable);
            if (errors != null && !errors.isEmpty()) {
                allErrors.addAll(errors);
            }
        }

        return ValidationResult.builder()
                .valid(allErrors.isEmpty())
                .errors(allErrors)
                .build();
    }
}
