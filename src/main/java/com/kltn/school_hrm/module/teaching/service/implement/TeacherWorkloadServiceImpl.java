package com.kltn.school_hrm.module.teaching.service.implement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.academic.entity.ClassSubject;
import com.kltn.school_hrm.module.academic.entity.Semester;
import com.kltn.school_hrm.module.academic.repository.ClassSubjectRepository;
import com.kltn.school_hrm.module.academic.repository.SemesterRepository;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.module.teaching.dto.response.TeacherWorkloadResponse;
import com.kltn.school_hrm.module.teaching.entity.TeachingAssignment;
import com.kltn.school_hrm.module.teaching.entity.TeachingNorm;
import com.kltn.school_hrm.module.teaching.repository.TeachingAssignmentRepository;
import com.kltn.school_hrm.module.teaching.repository.TeachingNormRepository;
import com.kltn.school_hrm.module.teaching.service.TeacherWorkloadService;
import com.kltn.school_hrm.shared.enums.Enums.TeachingAssignmentStatus;
import com.kltn.school_hrm.shared.exception.custom.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Service tính toán và theo dõi tải giảng dạy của giáo viên (BR-12).
 * Đối chiếu số tiết được phân công mỗi tuần với định mức giảng dạy (TeachingNorm).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherWorkloadServiceImpl implements TeacherWorkloadService {

    private final EmployeeRepository employeeRepository;
    private final SemesterRepository semesterRepository;
    private final TeachingAssignmentRepository assignmentRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final TeachingNormRepository teachingNormRepository;

    @Override
    public TeacherWorkloadResponse getTeacherWorkload(Long teacherId, Long semesterId) {
        Employee teacher = employeeRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giáo viên với id: " + teacherId));

        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học kỳ với id: " + semesterId));

        return calculateWorkloadForTeacher(teacher, semester);
    }

    @Override
    public List<TeacherWorkloadResponse> getWorkloadsBySemester(Long semesterId) {
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học kỳ với id: " + semesterId));

        List<Employee> allEmployees = employeeRepository.findAll();
        List<TeacherWorkloadResponse> result = new ArrayList<>();

        for (Employee teacher : allEmployees) {
            // Lọc các nhân viên có phân công giảng dạy
            List<TeachingAssignment> assignments = assignmentRepository.findByTeacherIdAndStatus(
                    teacher.getId(), TeachingAssignmentStatus.ACTIVE);
            if (!assignments.isEmpty()) {
                result.add(calculateWorkloadForTeacher(teacher, semester));
            }
        }

        return result;
    }

    private TeacherWorkloadResponse calculateWorkloadForTeacher(Employee teacher, Semester semester) {
        List<TeachingAssignment> assignments = assignmentRepository.findByTeacherIdAndStatus(
                teacher.getId(), TeachingAssignmentStatus.ACTIVE);

        double totalAssignedPeriods = 0.0;

        for (TeachingAssignment ta : assignments) {
            if (ta.getSchoolClass() != null && ta.getSubject() != null) {
                // Tra cứu số tiết yêu cầu trong học kỳ từ ClassSubject
                Optional<ClassSubject> csOpt = classSubjectRepository.findBySchoolClassIdAndSubjectIdAndSemesterId(
                        ta.getSchoolClass().getId(), ta.getSubject().getId(), semester.getId());
                if (csOpt.isPresent()) {
                    totalAssignedPeriods += csOpt.get().getPeriodsPerWeek();
                } else if (ta.getWeeklyContactHours() != null) {
                    totalAssignedPeriods += ta.getWeeklyContactHours();
                }
            } else if (ta.getWeeklyContactHours() != null) {
                totalAssignedPeriods += ta.getWeeklyContactHours();
            }
        }

        // Tìm định mức giảng dạy tương ứng
        String academicYearName = semester.getAcademicYear() != null ? semester.getAcademicYear().getName() : "";
        Long positionId = teacher.getPosition() != null ? teacher.getPosition().getId() : null;

        Integer standardHours = 18; // Mặc định phổ thông: 18 tiết/tuần
        Double effectiveNormHours = 18.0;

        if (positionId != null) {
            List<TeachingNorm> norms = teachingNormRepository.findByAcademicYear(academicYearName);
            for (TeachingNorm norm : norms) {
                if (norm.getPosition() != null && norm.getPosition().getId().equals(positionId)) {
                    if (norm.getStandardHours() != null) {
                        standardHours = norm.getStandardHours();
                        double reduction = 0.0;
                        if (norm.getReductionPercentage() != null) {
                            reduction = norm.getReductionPercentage().doubleValue();
                        }
                        effectiveNormHours = standardHours * (1.0 - (reduction / 100.0));
                    }
                    break;
                }
            }
        }

        double diff = totalAssignedPeriods - effectiveNormHours;
        String status;
        if (diff > 0.5) {
            status = "OVERLOAD"; // Vượt tải
        } else if (diff < -2.0) {
            status = "UNDERLOAD"; // Thiếu tải
        } else {
            status = "BALANCED"; // Cân đối
        }

        return TeacherWorkloadResponse.builder()
                .teacherId(teacher.getId())
                .teacherName(teacher.getFullName())
                .positionName(teacher.getPosition() != null ? teacher.getPosition().getName() : null)
                .academicYear(academicYearName)
                .semesterId(semester.getId())
                .semesterName(semester.getName())
                .totalAssignedPeriods(totalAssignedPeriods)
                .standardHours(standardHours)
                .effectiveNormHours(effectiveNormHours)
                .difference(diff)
                .workloadStatus(status)
                .build();
    }
}

