package com.kltn.school_hrm.module.scheduling.constraint.implement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.kltn.school_hrm.module.academic.entity.ClassSubject;
import com.kltn.school_hrm.module.academic.repository.ClassSubjectRepository;
import com.kltn.school_hrm.module.scheduling.constraint.ScheduleConstraint;
import com.kltn.school_hrm.module.scheduling.constraint.ValidationError;
import com.kltn.school_hrm.module.scheduling.entity.Timetable;
import com.kltn.school_hrm.module.scheduling.entity.TimetableEntry;
import com.kltn.school_hrm.module.scheduling.repository.TimetableEntryRepository;
import com.kltn.school_hrm.module.teaching.entity.TeachingAssignment;
import com.kltn.school_hrm.shared.enums.Enums.TeachingAssignmentStatus;

import lombok.RequiredArgsConstructor;

/**
 * BR-18: Kiểm tra số tiết môn học thừa và thiếu theo 2 chiều (SCH-02):
 * 1. Chiều chương trình học: Mỗi môn trong ClassSubject phải xếp đủ số tiết, không thừa không thiếu.
 * 2. Chiều thời khóa biểu: Mọi tiết học trong TKB phải thuộc môn học hợp lệ trong khung chương trình của lớp và kỳ.
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

        if (curriculumList.isEmpty()) {
            errors.add(ValidationError.builder()
                    .ruleId("BR-18")
                    .severity("ERROR")
                    .message(String.format("Lớp '%s' chưa được cấu hình chương trình môn học (ClassSubject) cho học kỳ '%s'",
                            timetable.getSchoolClass().getName(), timetable.getSemester().getName()))
                    .details(String.format("SchoolClassId: %d, SemesterId: %d",
                            timetable.getSchoolClass().getId(), timetable.getSemester().getId()))
                    .build());
            return errors;
        }

        List<TimetableEntry> entries = entryRepository.findByTimetableId(timetable.getId());

        // Tập hợp các môn học hợp lệ theo khung chương trình
        Set<Long> validSubjectIds = new HashSet<>();
        for (ClassSubject cs : curriculumList) {
            if (cs.getSubject() != null) {
                validSubjectIds.add(cs.getSubject().getId());
            }
        }

        // Đếm số tiết thực tế đã xếp theo từng Subject ID
        Map<Long, Integer> scheduledCounts = new HashMap<>();
        Map<Long, String> subjectNames = new HashMap<>();

        // ── Chiều 2: Rà soát từng entry trong thời khóa biểu ──
        for (TimetableEntry entry : entries) {
            TeachingAssignment ta = entry.getTeachingAssignment();
            if (ta == null) {
                errors.add(ValidationError.builder()
                        .ruleId("BR-18")
                        .severity("ERROR")
                        .entryId(entry.getId())
                        .message("Tiết học không có thông tin phân công giảng dạy (TeachingAssignment)")
                        .build());
                continue;
            }

            // Kiểm tra assignment có thuộc đúng lớp không
            if (ta.getSchoolClass() != null && !ta.getSchoolClass().getId().equals(timetable.getSchoolClass().getId())) {
                errors.add(ValidationError.builder()
                        .ruleId("BR-18")
                        .severity("ERROR")
                        .entryId(entry.getId())
                        .message(String.format("Tiết học chứa phân công thuộc lớp '%s' thay vì lớp '%s'",
                                ta.getSchoolClass().getName(), timetable.getSchoolClass().getName()))
                        .details(String.format("EntryId: %d, AssignmentId: %d", entry.getId(), ta.getId()))
                        .build());
            }

            // Kiểm tra trạng thái phân công
            if (ta.getStatus() != TeachingAssignmentStatus.ACTIVE) {
                errors.add(ValidationError.builder()
                        .ruleId("BR-18")
                        .severity("ERROR")
                        .entryId(entry.getId())
                        .message(String.format("Phân công môn '%s' đang ở trạng thái '%s', không hợp lệ",
                                ta.getSubjectName(), ta.getStatus()))
                        .build());
            }

            if (ta.getSubject() != null) {
                Long subjectId = ta.getSubject().getId();
                scheduledCounts.put(subjectId, scheduledCounts.getOrDefault(subjectId, 0) + 1);
                subjectNames.putIfAbsent(subjectId, ta.getSubject().getName());

                // Môn học xuất hiện trong TKB nhưng KHÔNG có trong ClassSubject của kỳ này
                if (!validSubjectIds.contains(subjectId)) {
                    errors.add(ValidationError.builder()
                            .ruleId("BR-18")
                            .severity("ERROR")
                            .entryId(entry.getId())
                            .message(String.format("Môn học '%s' có tiết học trong thời khóa biểu nhưng không thuộc chương trình học của lớp trong học kỳ này",
                                    ta.getSubject().getName()))
                            .details(String.format("SubjectId: %d, SchoolClassId: %d, SemesterId: %d",
                                    subjectId, timetable.getSchoolClass().getId(), timetable.getSemester().getId()))
                            .build());
                }
            }
        }

        // ── Chiều 1: Đối chiếu số tiết với khung chương trình đào tạo ──
        for (ClassSubject cs : curriculumList) {
            if (cs.getSubject() == null) {
                continue;
            }
            int required = cs.getPeriodsPerWeek() != null ? cs.getPeriodsPerWeek() : 0;
            int actual = scheduledCounts.getOrDefault(cs.getSubject().getId(), 0);

            if (actual < required) {
                errors.add(ValidationError.builder()
                        .ruleId("BR-18")
                        .severity("ERROR")
                        .message(String.format("Môn '%s' bị thiếu %d tiết (yêu cầu %d tiết/tuần, hiện mới xếp %d tiết)",
                                cs.getSubject().getName(), (required - actual), required, actual))
                        .details(String.format("SubjectId: %d, Required: %d, Actual: %d",
                                cs.getSubject().getId(), required, actual))
                        .build());
            } else if (actual > required) {
                errors.add(ValidationError.builder()
                        .ruleId("BR-18")
                        .severity("ERROR")
                        .message(String.format("Môn '%s' bị thừa %d tiết (yêu cầu %d tiết/tuần, nhưng đã xếp %d tiết)",
                                cs.getSubject().getName(), (actual - required), required, actual))
                        .details(String.format("SubjectId: %d, Required: %d, Actual: %d",
                                cs.getSubject().getId(), required, actual))
                        .build());
            }
        }

        return errors;
    }
}

