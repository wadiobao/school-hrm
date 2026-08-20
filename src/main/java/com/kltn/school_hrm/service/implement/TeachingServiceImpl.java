package com.kltn.school_hrm.service.implement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.request.TeachingLogRequest;
import com.kltn.school_hrm.dto.request.TeachingNormRequest;
import com.kltn.school_hrm.dto.response.TeachingLogResponse;
import com.kltn.school_hrm.dto.response.TeachingNormResponse;
import com.kltn.school_hrm.entity.core.Position;
import com.kltn.school_hrm.entity.employee.Employee;
import com.kltn.school_hrm.entity.teaching.TeachingAssignment;
import com.kltn.school_hrm.entity.teaching.TeachingLog;
import com.kltn.school_hrm.entity.teaching.TeachingNorm;
import com.kltn.school_hrm.enums.Enums.RequestStatus;
import com.kltn.school_hrm.exception.custom.ResourceNotFoundException;
import com.kltn.school_hrm.repository.EmployeeRepository;
import com.kltn.school_hrm.repository.PositionRepository;
import com.kltn.school_hrm.repository.TeachingAssignmentRepository;
import com.kltn.school_hrm.repository.TeachingLogRepository;
import com.kltn.school_hrm.repository.TeachingNormRepository;
import com.kltn.school_hrm.service.TeachingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeachingServiceImpl implements TeachingService {

    private final TeachingLogRepository teachingLogRepository;
    private final TeachingNormRepository teachingNormRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;

    // ===================== TeachingLog =====================

    @Override
    @Transactional
    public TeachingLogResponse createLog(TeachingLogRequest request) {
        TeachingAssignment assignment = teachingAssignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy phân công với id: " + request.getAssignmentId()));

        Employee actualTeacher = employeeRepository.findById(request.getActualTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy nhân viên với id: " + request.getActualTeacherId()));

        TeachingLog log = TeachingLog.builder()
                .assignment(assignment)
                .actualTeacher(actualTeacher)
                .teachingDate(request.getTeachingDate())
                .periodsTaught(request.getPeriodsTaught())
                .type(request.getType())
                .status(request.getStatus() != null ? request.getStatus() : RequestStatus.PENDING)
                .build();

        return mapToLogResponse(teachingLogRepository.save(log));
    }

    @Override
    @Transactional
    public TeachingLogResponse updateLog(Long id, TeachingLogRequest request) {
        TeachingLog log = teachingLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhật ký giảng dạy với id: " + id));

        TeachingAssignment assignment = teachingAssignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy phân công với id: " + request.getAssignmentId()));

        Employee actualTeacher = employeeRepository.findById(request.getActualTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy nhân viên với id: " + request.getActualTeacherId()));

        log.setAssignment(assignment);
        log.setActualTeacher(actualTeacher);
        log.setTeachingDate(request.getTeachingDate());
        log.setPeriodsTaught(request.getPeriodsTaught());
        log.setType(request.getType());
        if (request.getStatus() != null) {
            log.setStatus(request.getStatus());
        }

        return mapToLogResponse(teachingLogRepository.save(log));
    }

    @Override
    @Transactional(readOnly = true)
    public TeachingLogResponse getLogById(Long id) {
        return mapToLogResponse(teachingLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhật ký giảng dạy với id: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachingLogResponse> getAllLogs() {
        return teachingLogRepository.findAll().stream()
                .map(this::mapToLogResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachingLogResponse> getLogsByAssignmentId(Long assignmentId) {
        return teachingLogRepository.findByAssignmentId(assignmentId).stream()
                .map(this::mapToLogResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachingLogResponse> getLogsByTeacherId(Long teacherId) {
        return teachingLogRepository.findByActualTeacherId(teacherId).stream()
                .map(this::mapToLogResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachingLogResponse> getLogsByStatus(RequestStatus status) {
        return teachingLogRepository.findByStatus(status).stream()
                .map(this::mapToLogResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteLog(Long id) {
        if (!teachingLogRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy nhật ký giảng dạy với id: " + id);
        }
        teachingLogRepository.deleteById(id);
    }

    // ===================== TeachingNorm =====================

    @Override
    @Transactional
    public TeachingNormResponse createNorm(TeachingNormRequest request) {
        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy chức vụ với id: " + request.getPositionId()));

        TeachingNorm norm = TeachingNorm.builder()
                .academicYear(request.getAcademicYear())
                .position(position)
                .standardHours(request.getStandardHours())
                .reductionPercentage(request.getReductionPercentage())
                .build();

        return mapToNormResponse(teachingNormRepository.save(norm));
    }

    @Override
    @Transactional
    public TeachingNormResponse updateNorm(Long id, TeachingNormRequest request) {
        TeachingNorm norm = teachingNormRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy định mức giảng dạy với id: " + id));

        Position position = positionRepository.findById(request.getPositionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy chức vụ với id: " + request.getPositionId()));

        norm.setAcademicYear(request.getAcademicYear());
        norm.setPosition(position);
        norm.setStandardHours(request.getStandardHours());
        norm.setReductionPercentage(request.getReductionPercentage());

        return mapToNormResponse(teachingNormRepository.save(norm));
    }

    @Override
    @Transactional(readOnly = true)
    public TeachingNormResponse getNormById(Long id) {
        return mapToNormResponse(teachingNormRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy định mức giảng dạy với id: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachingNormResponse> getAllNorms() {
        return teachingNormRepository.findAll().stream()
                .map(this::mapToNormResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachingNormResponse> getNormsByAcademicYear(String academicYear) {
        return teachingNormRepository.findByAcademicYear(academicYear).stream()
                .map(this::mapToNormResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachingNormResponse> getNormsByPositionId(Long positionId) {
        return teachingNormRepository.findByPositionId(positionId).stream()
                .map(this::mapToNormResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteNorm(Long id) {
        if (!teachingNormRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy định mức giảng dạy với id: " + id);
        }
        teachingNormRepository.deleteById(id);
    }

    // ===================== Mappers =====================

    private TeachingLogResponse mapToLogResponse(TeachingLog log) {
        return TeachingLogResponse.builder()
                .id(log.getId())
                .assignmentId(log.getAssignment() != null ? log.getAssignment().getId() : null)
                .actualTeacherId(log.getActualTeacher() != null ? log.getActualTeacher().getId() : null)
                .teachingDate(log.getTeachingDate())
                .periodsTaught(log.getPeriodsTaught())
                .type(log.getType())
                .status(log.getStatus())
                .build();
    }

    private TeachingNormResponse mapToNormResponse(TeachingNorm norm) {
        return TeachingNormResponse.builder()
                .id(norm.getId())
                .academicYear(norm.getAcademicYear())
                .positionId(norm.getPosition() != null ? norm.getPosition().getId() : null)
                .standardHours(norm.getStandardHours())
                .reductionPercentage(norm.getReductionPercentage())
                .build();
    }
}
