package com.kltn.school_hrm.module.scheduling.service.implement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.academic.entity.Classroom;
import com.kltn.school_hrm.module.academic.repository.ClassSubjectRepository;
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
import com.kltn.school_hrm.module.teaching.repository.TeacherSubjectQualificationRepository;
import com.kltn.school_hrm.module.teaching.repository.TeachingAssignmentRepository;
import com.kltn.school_hrm.shared.enums.Enums.ClassroomStatus;
import com.kltn.school_hrm.shared.enums.Enums.EmployeeStatus;
import com.kltn.school_hrm.shared.enums.Enums.TeachingAssignmentStatus;
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
    private final ClassSubjectRepository classSubjectRepository;
    private final TeacherSubjectQualificationRepository qualificationRepository;
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

        // Kiểm tra tính hợp lệ của phân công, giáo viên và phòng học (SCH-01, SCH-04, SCH-05)
        validateAssignmentAndTeachers(timetable, assignment, substituteTeacher, classroom);

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

        // Kiểm tra tính hợp lệ của phân công, giáo viên và phòng học (SCH-01, SCH-04, SCH-05)
        validateAssignmentAndTeachers(timetable, assignment, substituteTeacher, classroom);

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

    /**
     * Kiểm tra tính hợp lệ nghiệp vụ của TeachingAssignment, Giáo viên chính, Giáo viên dạy thay và Phòng học.
     * Đáp ứng SCH-01, SCH-04, SCH-05 theo req.md.
     */
    private void validateAssignmentAndTeachers(Timetable timetable, TeachingAssignment assignment,
                                               Employee substituteTeacher, Classroom classroom) {
        // SCH-01.1: Phân công phải thuộc lớp của thời khóa biểu
        if (assignment.getSchoolClass() == null || timetable.getSchoolClass() == null
                || !assignment.getSchoolClass().getId().equals(timetable.getSchoolClass().getId())) {
            throw new BusinessException(String.format(
                    "Phân công giảng dạy môn '%s' không thuộc lớp '%s' của thời khóa biểu",
                    assignment.getSubjectName(),
                    timetable.getSchoolClass() != null ? timetable.getSchoolClass().getName() : "N/A"));
        }

        // SCH-01.2: Phân công giảng dạy phải ở trạng thái ACTIVE
        if (assignment.getStatus() != TeachingAssignmentStatus.ACTIVE) {
            throw new BusinessException(String.format(
                    "Phân công giảng dạy môn '%s' đang ở trạng thái '%s', không thể xếp vào thời khóa biểu",
                    assignment.getSubjectName(),
                    assignment.getStatus() != null ? assignment.getStatus().name() : "INACTIVE"));
        }

        // SCH-01.3: Giáo viên chính phải có trạng thái làm việc hợp lệ (WORKING)
        if (assignment.getTeacher() != null && assignment.getTeacher().getStatus() != EmployeeStatus.WORKING) {
            throw new BusinessException(String.format(
                    "Giáo viên chính '%s' hiện không ở trạng thái làm việc (%s)",
                    assignment.getTeacher().getFullName(),
                    assignment.getTeacher().getStatus() != null ? assignment.getTeacher().getStatus().name() : "N/A"));
        }

        // SCH-01.4: Môn học của assignment phải phù hợp với chương trình học của lớp trong học kỳ (ClassSubject)
        if (timetable.getSemester() != null && assignment.getSubject() != null && timetable.getSchoolClass() != null) {
            boolean subjectInCurriculum = classSubjectRepository.existsBySchoolClassIdAndSubjectIdAndSemesterId(
                    timetable.getSchoolClass().getId(), assignment.getSubject().getId(), timetable.getSemester().getId());
            if (!subjectInCurriculum) {
                throw new BusinessException(String.format(
                        "Môn '%s' không nằm trong chương trình học của lớp '%s' trong học kỳ '%s'",
                        assignment.getSubjectName(),
                        timetable.getSchoolClass().getName(),
                        timetable.getSemester().getName()));
            }
        }

        // SCH-04: Kiểm tra giáo viên dạy thay
        if (substituteTeacher != null) {
            // Không được trùng với giáo viên chính
            if (assignment.getTeacher() != null && substituteTeacher.getId().equals(assignment.getTeacher().getId())) {
                throw new BusinessException("Giáo viên dạy thay không được trùng với giáo viên chính của phân công");
            }

            // Trạng thái làm việc của giáo viên dạy thay
            if (substituteTeacher.getStatus() != EmployeeStatus.WORKING) {
                throw new BusinessException(String.format(
                        "Giáo viên dạy thay '%s' không ở trạng thái làm việc (%s)",
                        substituteTeacher.getFullName(),
                        substituteTeacher.getStatus() != null ? substituteTeacher.getStatus().name() : "N/A"));
            }

            // Chuyên môn phù hợp với môn học
            if (assignment.getSubject() != null) {
                boolean hasQualification = qualificationRepository.existsByTeacherIdAndSubjectId(
                        substituteTeacher.getId(), assignment.getSubject().getId());
                if (!hasQualification) {
                    throw new BusinessException(String.format(
                            "Giáo viên dạy thay '%s' không có chuyên môn / chứng chỉ hợp lệ môn '%s'",
                            substituteTeacher.getFullName(), assignment.getSubject().getName()));
                }
            }
        }

        // SCH-05: Kiểm tra điều kiện phòng học
        if (classroom != null) {
            if (classroom.getStatus() != null && classroom.getStatus() != ClassroomStatus.AVAILABLE) {
                throw new BusinessException(String.format(
                        "Phòng học '%s' đang ở trạng thái '%s', không thể sử dụng",
                        classroom.getName() != null ? classroom.getName() : classroom.getCode(),
                        classroom.getStatus().getLabel()));
            }

            if (classroom.getCapacity() != null && timetable.getSchoolClass() != null
                    && timetable.getSchoolClass().getCapacity() != null) {
                if (classroom.getCapacity() < timetable.getSchoolClass().getCapacity()) {
                    throw new BusinessException(String.format(
                            "Sức chứa của phòng '%s' (%d chỗ) không đủ cho sĩ số lớp '%s' (%d học sinh)",
                            classroom.getName() != null ? classroom.getName() : classroom.getCode(),
                            classroom.getCapacity(),
                            timetable.getSchoolClass().getName(),
                            timetable.getSchoolClass().getCapacity()));
                }
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

