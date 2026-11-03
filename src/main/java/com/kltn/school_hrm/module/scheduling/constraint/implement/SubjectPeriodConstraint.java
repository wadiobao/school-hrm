package com.kltn.school_hrm.module.scheduling.constraint.implement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.kltn.school_hrm.module.academic.entity.ClassSubject;
import com.kltn.school_hrm.module.academic.repository.ClassSubjectRepository;
import com.kltn.school_hrm.module.scheduling.constraint.ScheduleConstraint;
import com.kltn.school_hrm.module.scheduling.constraint.ValidationError;
import com.kltn.school_hrm.module.scheduling.entity.Timetable;
import com.kltn.school_hrm.module.scheduling.entity.TimetableEntry;
import com.kltn.school_hrm.module.scheduling.repository.TimetableEntryRepository;

import lombok.RequiredArgsConstructor;

/**
 * BR-18: Tổng số tiết đã xếp cho môn phải bằng số tiết yêu cầu mỗi tuần (ClassSubject.periodsPerWeek) trước khi công bố.
 */
@Component
@RequiredArgsConstructor
public class SubjectPeriodConstraint implements ScheduleConstraint {

    private final TimetableEntryRepository entryRepository;
    private final ClassSubjectRepository classSubjectRepository;

    @Override
    public List<ValidationError> validate(Timetable timetable) {
        List<ValidationError> errors = new ArrayList<>();
        if (timetable.getSchoolClass() == null || timetable.getSemester() == null) {
            return errors;
        }

        // Lấy danh sách môn học bắt buộc cho lớp trong học kỳ này
        List<ClassSubject> curriculumList = classSubjectRepository.findBySchoolClassIdAndSemesterId(
                timetable.getSchoolClass().getId(), timetable.getSemester().getId());

        List<TimetableEntry> entries = entryRepository.findByTimetableId(timetable.getId());

        // Đếm số tiết thực tế đã xếp theo từng Subject ID
        Map<Long, Integer> scheduledCounts = new HashMap<>();
        for (TimetableEntry entry : entries) {
            if (entry.getTeachingAssignment() != null && entry.getTeachingAssignment().getSubject() != null) {
                Long subjectId = entry.getTeachingAssignment().getSubject().getId();
                scheduledCounts.put(subjectId, scheduledCounts.getOrDefault(subjectId, 0) + 1);
            }
        }

        // Đối chiếu với yêu cầu khung chương trình
        for (ClassSubject cs : curriculumList) {
            if (cs.getSubject() == null) {
                continue;
            }
            int required = cs.getPeriodsPerWeek() != null ? cs.getPeriodsPerWeek() : 0;
            int actual = scheduledCounts.getOrDefault(cs.getSubject().getId(), 0);

            if (actual != required) {
                errors.add(ValidationError.builder()
                        .ruleId("BR-18")
                        .severity("ERROR")
                        .message(String.format("Môn '%s' yêu cầu %d tiết/tuần nhưng hiện tại mới xếp %d tiết",
                                cs.getSubject().getName(), required, actual))
                        .details(String.format("SubjectId: %d, Required: %d, Actual: %d",
                                cs.getSubject().getId(), required, actual))
                        .build());
            }
        }

        return errors;
    }
}

