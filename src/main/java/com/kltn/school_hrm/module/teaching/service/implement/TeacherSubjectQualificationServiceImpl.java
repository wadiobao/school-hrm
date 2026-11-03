package com.kltn.school_hrm.module.teaching.service.implement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.academic.entity.Subject;
import com.kltn.school_hrm.module.academic.repository.SubjectRepository;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.module.teaching.dto.request.TeacherSubjectQualificationRequest;
import com.kltn.school_hrm.module.teaching.dto.response.TeacherSubjectQualificationResponse;
import com.kltn.school_hrm.module.teaching.entity.TeacherSubjectQualification;
import com.kltn.school_hrm.module.teaching.repository.TeacherSubjectQualificationRepository;
import com.kltn.school_hrm.module.teaching.service.TeacherSubjectQualificationService;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;
import com.kltn.school_hrm.shared.exception.custom.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Service quản lý trình độ chuyên môn giảng dạy của giáo viên.
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TeacherSubjectQualificationServiceImpl implements TeacherSubjectQualificationService {

    private final TeacherSubjectQualificationRepository qualificationRepository;
    private final EmployeeRepository employeeRepository;
    private final SubjectRepository subjectRepository;

    @Override
    public TeacherSubjectQualificationResponse create(TeacherSubjectQualificationRequest request) {
        Employee teacher = employeeRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giáo viên với id: " + request.getTeacherId()));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với id: " + request.getSubjectId()));

        if (qualificationRepository.existsByTeacherIdAndSubjectId(request.getTeacherId(), request.getSubjectId())) {
            throw new BusinessException(String.format(
                    "Giáo viên '%s' đã được khai báo chuyên môn cho môn '%s'",
                    teacher.getFullName(), subject.getName()));
        }

        TeacherSubjectQualification entity = TeacherSubjectQualification.builder()
                .teacher(teacher)
                .subject(subject)
                .qualificationLevel(request.getQualificationLevel())
                .status(request.getStatus())
                .build();

        return mapToResponse(qualificationRepository.save(entity));
    }

    @Override
    public TeacherSubjectQualificationResponse update(Long id, TeacherSubjectQualificationRequest request) {
        TeacherSubjectQualification entity = qualificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên môn giảng dạy với id: " + id));

        Employee teacher = employeeRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giáo viên với id: " + request.getTeacherId()));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với id: " + request.getSubjectId()));

        entity.setTeacher(teacher);
        entity.setSubject(subject);
        entity.setQualificationLevel(request.getQualificationLevel());
        entity.setStatus(request.getStatus());

        return mapToResponse(qualificationRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherSubjectQualificationResponse getById(Long id) {
        return qualificationRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên môn giảng dạy với id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherSubjectQualificationResponse> getByTeacherId(Long teacherId) {
        return qualificationRepository.findByTeacherId(teacherId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherSubjectQualificationResponse> getBySubjectId(Long subjectId) {
        return qualificationRepository.findBySubjectId(subjectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (!qualificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy chuyên môn giảng dạy với id: " + id);
        }
        qualificationRepository.deleteById(id);
    }

    private TeacherSubjectQualificationResponse mapToResponse(TeacherSubjectQualification entity) {
        return TeacherSubjectQualificationResponse.builder()
                .id(entity.getId())
                .teacherId(entity.getTeacher() != null ? entity.getTeacher().getId() : null)
                .teacherName(entity.getTeacher() != null ? entity.getTeacher().getFullName() : null)
                .subjectId(entity.getSubject() != null ? entity.getSubject().getId() : null)
                .subjectCode(entity.getSubject() != null ? entity.getSubject().getCode() : null)
                .subjectName(entity.getSubject() != null ? entity.getSubject().getName() : null)
                .qualificationLevel(entity.getQualificationLevel())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

