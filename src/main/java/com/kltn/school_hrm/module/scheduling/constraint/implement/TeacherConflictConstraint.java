package com.kltn.school_hrm.module.scheduling.constraint.implement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.scheduling.constraint.ScheduleConstraint;
import com.kltn.school_hrm.module.scheduling.constraint.ValidationError;
import com.kltn.school_hrm.module.scheduling.entity.Timetable;
import com.kltn.school_hrm.module.scheduling.entity.TimetableEntry;
import com.kltn.school_hrm.module.scheduling.repository.TimetableEntryRepository;

import lombok.RequiredArgsConstructor;

/**
 * BR-15: Một giáo viên không được dạy hai lớp cùng một thời điểm.
 * Kiểm tra cả giáo viên chính trong TeachingAssignment và giáo viên dạy thay (substituteTeacher) nếu có.
 */
@Component
@RequiredArgsConstructor
public class TeacherConflictConstraint implements ScheduleConstraint {

    private final TimetableEntryRepository entryRepository;

    @Override
    public List<ValidationError> validate(Timetable timetable) {
        List<ValidationError> errors = new ArrayList<>();
        List<TimetableEntry> entries = entryRepository.findByTimetableId(timetable.getId());

        // 1. Kiểm tra xung đột nội bộ trong chính thời khóa biểu này
        Set<String> internalTeacherSlots = new HashSet<>();
        for (TimetableEntry entry : entries) {
            Employee effectiveTeacher = getEffectiveTeacher(entry);
            if (effectiveTeacher == null || entry.getTimeSlot() == null || entry.getDayOfWeek() == null) {
                continue;
            }

            String internalKey = effectiveTeacher.getId() + ":" + entry.getDayOfWeek() + ":" + entry.getTimeSlot().getId();
            if (!internalTeacherSlots.add(internalKey)) {
                errors.add(ValidationError.builder()
                        .ruleId("BR-15")
                        .severity("ERROR")
                        .entryId(entry.getId())
                        .message(String.format("Giáo viên '%s' bị xếp trùng 2 tiết trong cùng thời khóa biểu vào Thứ %d, Tiết %d",
                                effectiveTeacher.getFullName(), entry.getDayOfWeek() + 1, entry.getTimeSlot().getPeriodNumber()))
                        .details(String.format("TeacherId: %d", effectiveTeacher.getId()))
                        .build());
            }

            // 2. Kiểm tra xung đột với các thời khóa biểu khác đang hoạt động trong cùng học kỳ
            if (timetable.getSemester() != null) {
                long conflicts = entryRepository.countTeacherConflicts(
                        timetable.getSemester().getId(),
                        timetable.getId(),
                        effectiveTeacher.getId(),
                        entry.getDayOfWeek(),
                        entry.getTimeSlot().getId()
                );
                if (conflicts > 0) {
                    errors.add(ValidationError.builder()
                            .ruleId("BR-15")
                            .severity("ERROR")
                            .entryId(entry.getId())
                            .message(String.format("Giáo viên '%s' đã có lịch dạy ở lớp khác vào Thứ %d, Tiết %d trong học kỳ này",
                                    effectiveTeacher.getFullName(), entry.getDayOfWeek() + 1, entry.getTimeSlot().getPeriodNumber()))
                            .details(String.format("TeacherId: %d", effectiveTeacher.getId()))
                            .build());
                }
            }
        }

        return errors;
    }

    private Employee getEffectiveTeacher(TimetableEntry entry) {
        if (entry.getSubstituteTeacher() != null) {
            return entry.getSubstituteTeacher();
        }
        if (entry.getTeachingAssignment() != null) {
            return entry.getTeachingAssignment().getTeacher();
        }
        return null;
    }
}

