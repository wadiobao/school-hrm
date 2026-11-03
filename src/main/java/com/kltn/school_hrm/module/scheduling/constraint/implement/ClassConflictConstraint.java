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
 * BR-14: Một lớp không được có hai môn cùng một ngày và tiết học.
 */
@Component
@RequiredArgsConstructor
public class ClassConflictConstraint implements ScheduleConstraint {

    private final TimetableEntryRepository entryRepository;

    @Override
    public List<ValidationError> validate(Timetable timetable) {
        List<ValidationError> errors = new ArrayList<>();
        List<TimetableEntry> entries = entryRepository.findByTimetableId(timetable.getId());

        Set<String> seenSlots = new HashSet<>();
        for (TimetableEntry entry : entries) {
            if (entry.getTimeSlot() == null || entry.getDayOfWeek() == null) {
                continue;
            }
            String key = entry.getDayOfWeek() + ":" + entry.getTimeSlot().getId();
            if (!seenSlots.add(key)) {
                errors.add(ValidationError.builder()
                        .ruleId("BR-14")
                        .severity("ERROR")
                        .entryId(entry.getId())
                        .message(String.format("Lớp bị trùng 2 tiết học cùng vào Thứ %d, Tiết %d",
                                entry.getDayOfWeek() + 1, entry.getTimeSlot().getPeriodNumber()))
                        .details(String.format("TimeSlotId: %d", entry.getTimeSlot().getId()))
                        .build());
            }
        }

        return errors;
    }
}

