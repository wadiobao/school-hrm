package com.kltn.school_hrm.module.teaching.service.implement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.academic.entity.SchoolClass;
import com.kltn.school_hrm.module.academic.entity.Subject;
import com.kltn.school_hrm.module.academic.repository.SchoolClassRepository;
import com.kltn.school_hrm.module.academic.repository.SubjectRepository;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.module.teaching.dto.request.TeachingAssignmentRequest;
import com.kltn.school_hrm.module.teaching.dto.response.TeachingAssignmentHistoryResponse;
import com.kltn.school_hrm.module.teaching.dto.response.TeachingAssignmentResponse;
import com.kltn.school_hrm.module.teaching.entity.TeachingAssignment;
import com.kltn.school_hrm.module.teaching.entity.TeachingAssignmentHistory;
import com.kltn.school_hrm.module.teaching.repository.TeacherSubjectQualificationRepository;
import com.kltn.school_hrm.module.teaching.repository.TeachingAssignmentHistoryRepository;
import com.kltn.school_hrm.module.teaching.repository.TeachingAssignmentRepository;
import com.kltn.school_hrm.module.teaching.service.TeachingAssignmentService;
import com.kltn.school_hrm.shared.enums.Enums.Curriculum;
import com.kltn.school_hrm.shared.enums.Enums.EmployeeStatus;
import com.kltn.school_hrm.shared.enums.Enums.SubjectStatus;
import com.kltn.school_hrm.shared.enums.Enums.TeachingAssignmentStatus;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;
import com.kltn.school_hrm.shared.exception.custom.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Service quản lý phân công giảng dạy cho giáo viên.
 * Đảm bảo các quy tắc:
 * - BR-07: Giáo viên phải tồn tại trong Employee.
 * - BR-08: Giáo viên phải có trạng thái làm việc hợp lệ (WORKING).
 * - BR-09: Giáo viên phải có chuyên môn phù hợp với môn được phân công.
 * - Quy tắc người dùng: Mỗi lớp - môn học chỉ có 1 giáo viên phụ trách chính trong 1 năm học.
 * - Lưu vết lịch sử qua TeachingAssignmentHistory khi đổi giáo viên.
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TeachingAssignmentServiceImpl implements TeachingAssignmentService {

    private final TeachingAssignmentRepository assignmentRepository;
    private final TeachingAssignmentHistoryRepository historyRepository;
    private final TeacherSubjectQualificationRepository qualificationRepository;
    private final EmployeeRepository employeeRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SubjectRepository subjectRepository;

    @Override
    public TeachingAssignmentResponse create(TeachingAssignmentRequest request) {
        Employee teacher = employeeRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giáo viên với id: " + request.getTeacherId()));

        // BR-08: Trạng thái làm việc của giáo viên
        validateTeacherStatus(teacher);

        SchoolClass schoolClass = null;
        if (request.getSchoolClassId() != null) {
            schoolClass = schoolClassRepository.findById(request.getSchoolClassId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với id: " + request.getSchoolClassId()));
        }

        Subject subject = null;
        if (request.getSubjectId() != null) {
            subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với id: " + request.getSubjectId()));

            // BR-09: Giáo viên phải có chuyên môn phù hợp
            validateTeacherQualification(teacher.getId(), subject.getId(), teacher.getFullName(), subject.getName());
        }

        // Quy tắc: Trong 1 năm học, mỗi lớp - môn học chỉ có 1 giáo viên phụ trách chính
        if (schoolClass != null && subject != null) {
            if (assignmentRepository.existsBySchoolClassIdAndSubjectId(schoolClass.getId(), subject.getId())) {
                throw new BusinessException(String.format(
                        "Lớp '%s' đã có giáo viên phụ trách môn '%s' trong năm học này",
                        schoolClass.getName(), subject.getName()));
            }
        }

        String resolvedSubjectName = subject != null ? subject.getName() : request.getSubjectName();
        String resolvedGradeLevel = schoolClass != null ? String.valueOf(schoolClass.getGradeLevel()) : request.getGradeLevel();

        TeachingAssignment entity = TeachingAssignment.builder()
                .schoolClass(schoolClass)
                .subject(subject)
                .teacher(teacher)
                .curriculum(request.getCurriculum())
                .subjectName(resolvedSubjectName)
                .gradeLevel(resolvedGradeLevel)
                .weeklyContactHours(request.getWeeklyContactHours())
                .assignedAt(LocalDateTime.now())
                .status(request.getStatus() != null ? request.getStatus() : TeachingAssignmentStatus.ACTIVE)
                .build();

        return mapToResponse(assignmentRepository.save(entity));
    }

    @Override
    public TeachingAssignmentResponse update(Long id, TeachingAssignmentRequest request) {
        TeachingAssignment entity = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân công với id: " + id));

        Employee newTeacher = employeeRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giáo viên với id: " + request.getTeacherId()));

        validateTeacherStatus(newTeacher);

        SchoolClass schoolClass = entity.getSchoolClass();
        if (request.getSchoolClassId() != null) {
            schoolClass = schoolClassRepository.findById(request.getSchoolClassId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với id: " + request.getSchoolClassId()));
        }

        Subject subject = entity.getSubject();
        if (request.getSubjectId() != null) {
            subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với id: " + request.getSubjectId()));
            validateTeacherQualification(newTeacher.getId(), subject.getId(), newTeacher.getFullName(), subject.getName());
        }

        if (schoolClass != null && subject != null) {
            if (assignmentRepository.existsBySchoolClassIdAndSubjectIdAndIdNot(schoolClass.getId(), subject.getId(), id)) {
                throw new BusinessException(String.format(
                        "Lớp '%s' đã có giáo viên phụ trách môn '%s' trong năm học này",
                        schoolClass.getName(), subject.getName()));
            }
        }

        // Lưu vết lịch sử nếu giáo viên thay đổi
        Employee oldTeacher = entity.getTeacher();
        if (oldTeacher != null && !oldTeacher.getId().equals(newTeacher.getId())) {
            TeachingAssignmentHistory history = TeachingAssignmentHistory.builder()
                    .teachingAssignment(entity)
                    .oldTeacher(oldTeacher)
                    .newTeacher(newTeacher)
                    .changedAt(LocalDateTime.now())
                    .reason(request.getTransferReason() != null ? request.getTransferReason() : "Chuyển giao phân công giáo viên")
                    .build();
            historyRepository.save(history);
        }

        entity.setSchoolClass(schoolClass);
        entity.setSubject(subject);
        entity.setTeacher(newTeacher);
        entity.setCurriculum(request.getCurriculum());
        entity.setSubjectName(subject != null ? subject.getName() : request.getSubjectName());
        entity.setGradeLevel(schoolClass != null ? String.valueOf(schoolClass.getGradeLevel()) : request.getGradeLevel());
        entity.setWeeklyContactHours(request.getWeeklyContactHours());
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }

        return mapToResponse(assignmentRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public TeachingAssignmentResponse getById(Long id) {
        return assignmentRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân công với id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachingAssignmentResponse> getAll() {
        return assignmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachingAssignmentResponse> getByTeacherId(Long teacherId) {
        return assignmentRepository.findByTeacherId(teacherId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachingAssignmentResponse> getByTeacherIdAndCurriculum(Long teacherId, Curriculum curriculum) {
        return assignmentRepository.findByTeacherIdAndCurriculum(teacherId, curriculum).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachingAssignmentResponse> getBySchoolClassId(Long schoolClassId) {
        return assignmentRepository.findBySchoolClassId(schoolClassId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachingAssignmentHistoryResponse> getAssignmentHistory(Long assignmentId) {
        return historyRepository.findByTeachingAssignmentIdOrderByChangedAtDesc(assignmentId).stream()
                .map(h -> TeachingAssignmentHistoryResponse.builder()
                        .id(h.getId())
                        .teachingAssignmentId(h.getTeachingAssignment().getId())
                        .oldTeacherId(h.getOldTeacher() != null ? h.getOldTeacher().getId() : null)
                        .oldTeacherName(h.getOldTeacher() != null ? h.getOldTeacher().getFullName() : null)
                        .newTeacherId(h.getNewTeacher() != null ? h.getNewTeacher().getId() : null)
                        .newTeacherName(h.getNewTeacher() != null ? h.getNewTeacher().getFullName() : null)
                        .changedAt(h.getChangedAt())
                        .reason(h.getReason())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy phân công với id: " + id);
        }
        assignmentRepository.deleteById(id);
    }

    private void validateTeacherStatus(Employee teacher) {
        if (teacher.getStatus() != null && teacher.getStatus() != EmployeeStatus.WORKING && teacher.getStatus() != EmployeeStatus.PROBATION) {
            throw new BusinessException(String.format(
                    "Giáo viên '%s' không ở trạng thái làm việc hợp lệ (%s)",
                    teacher.getFullName(), teacher.getStatus().getLabel()));
        }
    }

    private void validateTeacherQualification(Long teacherId, Long subjectId, String teacherName, String subjectName) {
        boolean qualified = qualificationRepository.existsByTeacherIdAndSubjectIdAndStatus(
                teacherId, subjectId, SubjectStatus.ACTIVE);
        if (!qualified) {
            // Nếu bảng qualification chưa có dữ liệu thì không chặn hoàn toàn, hoặc cảnh báo nếu đã có ít nhất 1 qualification trong hệ thống
            long totalQualifications = qualificationRepository.count();
            if (totalQualifications > 0) {
                throw new BusinessException(String.format(
                        "Giáo viên '%s' chưa được cấp chứng nhận chuyên môn hợp lệ cho môn '%s' (BR-09)",
                        teacherName, subjectName));
            }
        }
    }

    private TeachingAssignmentResponse mapToResponse(TeachingAssignment entity) {
        return TeachingAssignmentResponse.builder()
                .id(entity.getId())
                .schoolClassId(entity.getSchoolClass() != null ? entity.getSchoolClass().getId() : null)
                .schoolClassName(entity.getSchoolClass() != null ? entity.getSchoolClass().getName() : null)
                .subjectId(entity.getSubject() != null ? entity.getSubject().getId() : null)
                .subjectCode(entity.getSubject() != null ? entity.getSubject().getCode() : null)
                .subjectName(entity.getSubjectName() != null ? entity.getSubjectName() :
                        (entity.getSubject() != null ? entity.getSubject().getName() : null))
                .teacherId(entity.getTeacher() != null ? entity.getTeacher().getId() : null)
                .teacherName(entity.getTeacher() != null ? entity.getTeacher().getFullName() : null)
                .curriculum(entity.getCurriculum())
                .gradeLevel(entity.getGradeLevel())
                .weeklyContactHours(entity.getWeeklyContactHours())
                .status(entity.getStatus())
                .assignedAt(entity.getAssignedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
