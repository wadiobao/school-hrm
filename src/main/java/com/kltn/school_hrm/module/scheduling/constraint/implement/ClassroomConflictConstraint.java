package com.kltn.school_hrm.module.scheduling.constraint.implement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.kltn.school_hrm.module.scheduling.constraint.ScheduleConstraint;
import com.kltn.school_hrm.module.scheduling.constraint.ValidationError;
import com.kltn.school_hrm.module.scheduling.entity.Timetable;
import com.kltn.school_hrm.module.scheduling.entity.TimetableEntry;
import com.kltn.school_hrm.module.scheduling.repository.TimetableEntryRepository;

import lombok.RequiredArgsConstructor;

/**
 * BR-16: Một phòng học không được sử dụng bởi hai lớp cùng thời điểm.
 */
@Component
@RequiredArgsConstructor
public class ClassroomConflictConstraint implements ScheduleConstraint {

    private final TimetableEntryRepository entryRepository;

    @Override
    public List<ValidationError> validate(Timetable timetable) {
        List<ValidationError> errors = new ArrayList<>();
        List<TimetableEntry> entries = entryRepository.findByTimetableId(timetable.getId());

        Set<String> internalRoomSlots = new HashSet<>();
        for (TimetableEntry entry : entries) {
            if (entry.getClassroom() == null || entry.getTimeSlot() == null || entry.getDayOfWeek() == null) {
                continue;
            }

            String internalKey = entry.getClassroom().getId() + ":" + entry.getDayOfWeek() + ":" + entry.getTimeSlot().getId();
            if (!internalRoomSlots.add(internalKey)) {
                errors.add(ValidationError.builder()
                        .ruleId("BR-16")
                        .severity("ERROR")
                        .entryId(entry.getId())
                        .message(String.format("Phòng '%s' bị xếp trùng cho 2 tiết trong cùng thời khóa biểu vào Thứ %d, Tiết %d",
                                entry.getClassroom().getCode(), entry.getDayOfWeek() + 1, entry.getTimeSlot().getPeriodNumber()))
                        .details(String.format("ClassroomId: %d", entry.getClassroom().getId()))
                        .build());
            }

            if (timetable.getSemester() != null) {
                long conflicts = entryRepository.countClassroomConflicts(
                        timetable.getSemester().getId(),
                        timetable.getId(),
                        entry.getClassroom().getId(),
                        entry.getDayOfWeek(),
                        entry.getTimeSlot().getId()
                );
                if (conflicts > 0) {
                    errors.add(ValidationError.builder()
                            .ruleId("BR-16")
                            .severity("ERROR")
                            .entryId(entry.getId())
                            .message(String.format("Phòng '%s' đã được xếp cho lớp khác vào Thứ %d, Tiết %d trong học kỳ này",
                                    entry.getClassroom().getCode(), entry.getDayOfWeek() + 1, entry.getTimeSlot().getPeriodNumber()))
                            .details(String.format("ClassroomId: %d", entry.getClassroom().getId()))
                            .build());
                }
            }
        }

        return errors;
    }
}

