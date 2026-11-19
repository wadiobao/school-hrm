package com.kltn.school_hrm.module.scheduling.constraint.implement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.kltn.school_hrm.module.academic.entity.Classroom;
import com.kltn.school_hrm.module.scheduling.constraint.ScheduleConstraint;
import com.kltn.school_hrm.module.scheduling.constraint.ValidationError;
import com.kltn.school_hrm.module.scheduling.entity.Timetable;
import com.kltn.school_hrm.module.scheduling.entity.TimetableEntry;
import com.kltn.school_hrm.module.scheduling.repository.TimetableEntryRepository;
import com.kltn.school_hrm.shared.enums.Enums.ClassroomStatus;

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

            Classroom room = entry.getClassroom();

            // SCH-05: Kiểm tra trạng thái hoạt động của phòng học
            if (room.getStatus() != null && room.getStatus() != ClassroomStatus.AVAILABLE) {
                errors.add(ValidationError.builder()
                        .ruleId("BR-16")
                        .severity("ERROR")
                        .entryId(entry.getId())
                        .message(String.format("Phòng '%s' đang ở trạng thái '%s', không khả dụng để xếp lịch",
                                room.getName() != null ? room.getName() : room.getCode(), room.getStatus().getLabel()))
                        .details(String.format("ClassroomId: %d, Status: %s", room.getId(), room.getStatus()))
                        .build());
            }

            // SCH-05: Kiểm tra sức chứa phòng so với sĩ số lớp
            if (room.getCapacity() != null && timetable.getSchoolClass() != null
                    && timetable.getSchoolClass().getCapacity() != null) {
                if (room.getCapacity() < timetable.getSchoolClass().getCapacity()) {
                    errors.add(ValidationError.builder()
                            .ruleId("BR-16")
                            .severity("ERROR")
                            .entryId(entry.getId())
                            .message(String.format("Sức chứa của phòng '%s' (%d chỗ) không đủ cho sĩ số lớp '%s' (%d học sinh)",
                                    room.getName() != null ? room.getName() : room.getCode(),
                                    room.getCapacity(),
                                    timetable.getSchoolClass().getName(),
                                    timetable.getSchoolClass().getCapacity()))
                            .details(String.format("ClassroomId: %d, RoomCapacity: %d, ClassCapacity: %d",
                                    room.getId(), room.getCapacity(), timetable.getSchoolClass().getCapacity()))
                            .build());
                }
            }

            String internalKey = room.getId() + ":" + entry.getDayOfWeek() + ":" + entry.getTimeSlot().getId();
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

