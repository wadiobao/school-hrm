package com.kltn.school_hrm.module.scheduling.service.implement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.academic.entity.Classroom;
import com.kltn.school_hrm.module.academic.repository.ClassroomRepository;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.module.scheduling.dto.request.TimetableEntryRequest;
import com.kltn.school_hrm.module.scheduling.dto.response.TimetableEntryResponse;
import com.kltn.school_hrm.module.scheduling.entity.TimeSlot;
import com.kltn.school_hrm.module.scheduling.entity.Timetable;
import com.kltn.school_hrm.module.scheduling.entity.TimetableEntry;
import com.kltn.school_hrm.module.scheduling.repository.TimeSlotRepository;
import com.kltn.school_hrm.module.scheduling.repository.TimetableEntryRepository;
import com.kltn.school_hrm.module.scheduling.repository.TimetableRepository;
import com.kltn.school_hrm.module.scheduling.service.TimetableEntryService;
import com.kltn.school_hrm.module.scheduling.statemachine.TimetableStateMachine;
import com.kltn.school_hrm.module.teaching.entity.TeachingAssignment;
import com.kltn.school_hrm.module.teaching.repository.TeachingAssignmentRepository;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;
import com.kltn.school_hrm.shared.exception.custom.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Service quản lý các tiết học cụ thể trên thời khóa biểu.
 * Kiểm tra tính hợp lệ và xung đột thời gian thực trước khi lưu.
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TimetableEntryServiceImpl implements TimetableEntryService {

    private final TimetableEntryRepository entryRepository;
    private final TimetableRepository timetableRepository;
    private final TeachingAssignmentRepository assignmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ClassroomRepository classroomRepository;
    private final EmployeeRepository employeeRepository;
    private final TimetableStateMachine stateMachine;

    @Override
    public TimetableEntryResponse create(TimetableEntryRequest request) {
        Timetable timetable = timetableRepository.findById(request.getTimetableId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thời khóa biểu với id: " + request.getTimetableId()));

        // BR-22: Không được sửa trực tiếp thời khóa biểu không ở trạng thái DRAFT
        if (!stateMachine.canModifyEntries(timetable.getStatus())) {
            throw new BusinessException(String.format(
                    "Không thể thêm tiết học khi thời khóa biểu ở trạng thái '%s'",
                    timetable.getStatus().getLabel()));
        }

        TeachingAssignment assignment = assignmentRepository.findById(request.getTeachingAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân công giảng dạy với id: " + request.getTeachingAssignmentId()));

        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tiết học với id: " + request.getTimeSlotId()));

        Classroom classroom = null;
        if (request.getClassroomId() != null) {
            classroom = classroomRepository.findById(request.getClassroomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng học với id: " + request.getClassroomId()));
        }

        Employee substituteTeacher = null;
        if (request.getSubstituteTeacherId() != null) {
            substituteTeacher = employeeRepository.findById(request.getSubstituteTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giáo viên thay thế với id: " + request.getSubstituteTeacherId()));
        }

        // Kiểm tra xung đột trước khi insert
        validateConflicts(timetable, assignment, substituteTeacher, classroom, request.getDayOfWeek(), timeSlot, null);

        TimetableEntry entity = TimetableEntry.builder()
                .timetable(timetable)
                .teachingAssignment(assignment)
                .substituteTeacher(substituteTeacher)
                .dayOfWeek(request.getDayOfWeek())
                .timeSlot(timeSlot)
                .classroom(classroom)
                .build();

        return mapToResponse(entryRepository.save(entity));
    }

    @Override
    public TimetableEntryResponse update(Long id, TimetableEntryRequest request) {
        TimetableEntry entity = entryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tiết học với id: " + id));

        Timetable timetable = entity.getTimetable();
        if (!stateMachine.canModifyEntries(timetable.getStatus())) {
            throw new BusinessException(String.format(
                    "Không thể điều chỉnh tiết học khi thời khóa biểu ở trạng thái '%s'",
                    timetable.getStatus().getLabel()));
        }

        TeachingAssignment assignment = assignmentRepository.findById(request.getTeachingAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân công giảng dạy với id: " + request.getTeachingAssignmentId()));

        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tiết học với id: " + request.getTimeSlotId()));

        Classroom classroom = null;
        if (request.getClassroomId() != null) {
            classroom = classroomRepository.findById(request.getClassroomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng học với id: " + request.getClassroomId()));
        }

        Employee substituteTeacher = null;
        if (request.getSubstituteTeacherId() != null) {
            substituteTeacher = employeeRepository.findById(request.getSubstituteTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giáo viên thay thế với id: " + request.getSubstituteTeacherId()));
        }

        validateConflicts(timetable, assignment, substituteTeacher, classroom, request.getDayOfWeek(), timeSlot, id);

        entity.setTeachingAssignment(assignment);
        entity.setSubstituteTeacher(substituteTeacher);
        entity.setDayOfWeek(request.getDayOfWeek());
        entity.setTimeSlot(timeSlot);
        entity.setClassroom(classroom);

        return mapToResponse(entryRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public TimetableEntryResponse getById(Long id) {
        return entryRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tiết học với id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableEntryResponse> getByTimetableId(Long timetableId) {
        return entryRepository.findByTimetableIdOrderByDayOfWeekAscTimeSlotPeriodNumberAsc(timetableId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        TimetableEntry entity = entryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tiết học với id: " + id));

        if (!stateMachine.canModifyEntries(entity.getTimetable().getStatus())) {
            throw new BusinessException(String.format(
                    "Không thể xóa tiết học khi thời khóa biểu ở trạng thái '%s'",
                    entity.getTimetable().getStatus().getLabel()));
        }

        entryRepository.deleteById(id);
    }

    private void validateConflicts(Timetable timetable, TeachingAssignment assignment,
                                   Employee substituteTeacher, Classroom classroom,
                                   Integer dayOfWeek, TimeSlot timeSlot, Long excludeEntryId) {
        // BR-14: Trùng lớp tại khung giờ này
        long classConflicts = entryRepository.countClassConflicts(
                timetable.getId(), dayOfWeek, timeSlot.getId(), excludeEntryId);
        if (classConflicts > 0) {
            throw new BusinessException(String.format(
                    "Lớp '%s' đã có tiết học khác vào Thứ %d, Tiết %d",
                    timetable.getSchoolClass().getName(), dayOfWeek + 1, timeSlot.getPeriodNumber()));
        }

        // BR-15: Trùng giáo viên (giáo viên chính hoặc giáo viên dạy thay)
        Employee effectiveTeacher = substituteTeacher != null ? substituteTeacher : assignment.getTeacher();
        if (effectiveTeacher != null && timetable.getSemester() != null) {
            long teacherConflicts = entryRepository.countTeacherConflicts(
                    timetable.getSemester().getId(),
                    timetable.getId(),
                    effectiveTeacher.getId(),
                    dayOfWeek,
                    timeSlot.getId());
            if (teacherConflicts > 0) {
                throw new BusinessException(String.format(
                        "Giáo viên '%s' đã có lịch dạy ở lớp khác vào Thứ %d, Tiết %d trong học kỳ này",
                        effectiveTeacher.getFullName(), dayOfWeek + 1, timeSlot.getPeriodNumber()));
            }
        }

        // BR-16: Trùng phòng học
        if (classroom != null && timetable.getSemester() != null) {
            long roomConflicts = entryRepository.countClassroomConflicts(
                    timetable.getSemester().getId(),
                    timetable.getId(),
                    classroom.getId(),
                    dayOfWeek,
                    timeSlot.getId());
            if (roomConflicts > 0) {
                throw new BusinessException(String.format(
                        "Phòng '%s' đã được xếp cho lớp khác vào Thứ %d, Tiết %d",
                        classroom.getCode(), dayOfWeek + 1, timeSlot.getPeriodNumber()));
            }
        }
    }

    private TimetableEntryResponse mapToResponse(TimetableEntry entity) {
        TeachingAssignment ta = entity.getTeachingAssignment();
        TimeSlot slot = entity.getTimeSlot();
        Classroom room = entity.getClassroom();

        String dayLabel = entity.getDayOfWeek() != null ?
                (entity.getDayOfWeek() == 7 ? "Chủ nhật" : "Thứ " + (entity.getDayOfWeek() + 1)) : "";

        String slotRange = slot != null ? (slot.getStartTime() + " - " + slot.getEndTime()) : "";

        return TimetableEntryResponse.builder()
                .id(entity.getId())
                .timetableId(entity.getTimetable() != null ? entity.getTimetable().getId() : null)
                .teachingAssignmentId(ta != null ? ta.getId() : null)
                .subjectName(ta != null ? ta.getSubjectName() : null)
                .subjectCode(ta != null && ta.getSubject() != null ? ta.getSubject().getCode() : null)
                .teacherId(ta != null && ta.getTeacher() != null ? ta.getTeacher().getId() : null)
                .teacherName(ta != null && ta.getTeacher() != null ? ta.getTeacher().getFullName() : null)
                .substituteTeacherId(entity.getSubstituteTeacher() != null ? entity.getSubstituteTeacher().getId() : null)
                .substituteTeacherName(entity.getSubstituteTeacher() != null ? entity.getSubstituteTeacher().getFullName() : null)
                .dayOfWeek(entity.getDayOfWeek())
                .dayOfWeekLabel(dayLabel)
                .timeSlotId(slot != null ? slot.getId() : null)
                .periodNumber(slot != null ? slot.getPeriodNumber() : null)
                .timeSlotRange(slotRange)
                .classroomId(room != null ? room.getId() : null)
                .classroomCode(room != null ? room.getCode() : null)
                .classroomName(room != null ? room.getName() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
