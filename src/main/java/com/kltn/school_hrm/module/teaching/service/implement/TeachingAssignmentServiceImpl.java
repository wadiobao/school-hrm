package com.kltn.school_hrm.module.teaching.service.implement;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kltn.school_hrm.module.teaching.dto.request.TeachingAssignmentRequest;
import com.kltn.school_hrm.module.teaching.dto.response.TeachingAssignmentResponse;
import com.kltn.school_hrm.shared.enums.Enums.Curriculum;
import com.kltn.school_hrm.module.teaching.service.TeachingAssignmentService;

import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.module.teaching.entity.TeachingAssignment;
import com.kltn.school_hrm.module.teaching.repository.TeachingAssignmentRepository;
import com.kltn.school_hrm.shared.exception.custom.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TeachingAssignmentServiceImpl implements TeachingAssignmentService {

    private final TeachingAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public TeachingAssignmentResponse create(TeachingAssignmentRequest request) {
        Employee teacher = employeeRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giáo viên với id: " + request.getTeacherId()));

        TeachingAssignment entity = TeachingAssignment.builder()
                .teacher(teacher)
                .curriculum(request.getCurriculum())
                .subjectName(request.getSubjectName())
                .gradeLevel(request.getGradeLevel())
                .weeklyContactHours(request.getWeeklyContactHours())
                .build();

        return mapToResponse(assignmentRepository.save(entity));
    }

    @Override
    @Transactional
    public TeachingAssignmentResponse update(Long id, TeachingAssignmentRequest request) {
        TeachingAssignment entity = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân công với id: " + id));

        Employee teacher = employeeRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giáo viên với id: " + request.getTeacherId()));

        entity.setTeacher(teacher);
        entity.setCurriculum(request.getCurriculum());
        entity.setSubjectName(request.getSubjectName());
        entity.setGradeLevel(request.getGradeLevel());
        entity.setWeeklyContactHours(request.getWeeklyContactHours());

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
    @Transactional
    public void delete(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy phân công với id: " + id);
        }
        assignmentRepository.deleteById(id);
    }

    private TeachingAssignmentResponse mapToResponse(TeachingAssignment entity) {
        return TeachingAssignmentResponse.builder()
                .id(entity.getId())
                .teacherId(entity.getTeacher() != null ? entity.getTeacher().getId() : null)
                .curriculum(entity.getCurriculum())
                .subjectName(entity.getSubjectName())
                .gradeLevel(entity.getGradeLevel())
                .weeklyContactHours(entity.getWeeklyContactHours())
                .build();
    }
}
