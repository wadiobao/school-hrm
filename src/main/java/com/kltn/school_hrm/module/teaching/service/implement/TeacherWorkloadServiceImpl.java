package com.kltn.school_hrm.module.teaching.service.implement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.academic.entity.ClassSubject;
import com.kltn.school_hrm.module.academic.entity.SchoolClass;
import com.kltn.school_hrm.module.academic.entity.Semester;
import com.kltn.school_hrm.module.academic.repository.ClassSubjectRepository;
import com.kltn.school_hrm.module.academic.repository.SchoolClassRepository;
import com.kltn.school_hrm.module.academic.repository.SemesterRepository;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.module.teaching.dto.response.TeacherWorkloadResponse;
import com.kltn.school_hrm.module.teaching.entity.TeachingAssignment;
import com.kltn.school_hrm.module.teaching.entity.TeachingNorm;
import com.kltn.school_hrm.module.teaching.repository.TeachingAssignmentRepository;
import com.kltn.school_hrm.module.teaching.repository.TeachingNormRepository;
import com.kltn.school_hrm.module.teaching.service.TeacherWorkloadService;
import com.kltn.school_hrm.shared.enums.Enums.ClassStatus;
import com.kltn.school_hrm.shared.enums.Enums.PositionCode;
import com.kltn.school_hrm.shared.enums.Enums.TeachingAssignmentStatus;
import com.kltn.school_hrm.shared.exception.custom.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Service tính toán và theo dõi tải giảng dạy của giáo viên theo Thông tư 05/2025/TT-BGDĐT & Thông tư 21/2025/TT-BGDĐT.
 * <p>
 * Công thức:
 * - Định mức chuẩn giáo viên THCS: 19 tiết/tuần (665 tiết/năm học 35 tuần).
 * - Hiệu trưởng: 2 tiết/tuần | Phó hiệu trưởng: 4 tiết/tuần.
 * - Giảm trừ kiêm nhiệm (tối đa 2 nhiệm vụ cao nhất):
 *   + Giáo viên chủ nhiệm lớp (GVCN): giảm 4 tiết/tuần.
 *   + Tổ trưởng / Trưởng bộ môn: giảm 3 tiết/tuần.
 * - ĐM_thực = ĐM_chuẩn - (LARGE(F2:H2, 1) + LARGE(F2:H2, 2))
 * - Chênh lệch tuần = T_giao - ĐM_thực
 * - Tổng thừa/thiếu cả năm (35 tuần) = (T_giao - ĐM_thực) * 35 (Trần thanh toán thừa giờ: 200 tiết/năm).
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherWorkloadServiceImpl implements TeacherWorkloadService {

    private static final Logger log = LoggerFactory.getLogger(TeacherWorkloadServiceImpl.class);

    public static final int DEFAULT_STANDARD_NORM_THCS = 19;
    public static final int WEEKS_PER_ACADEMIC_YEAR = 35;
    public static final int MAX_PAID_OVERTIME_PERIODS_PER_YEAR = 200;
    public static final double MAX_WEEKLY_OVERTIME_50_PCT = 9.5;

    private final EmployeeRepository employeeRepository;
    private final SemesterRepository semesterRepository;
    private final SchoolClassRepository schoolClassRepository;
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
                if (ta.getSchoolClass().getAcademicYear() != null && semester.getAcademicYear() != null) {
                    if (!ta.getSchoolClass().getAcademicYear().getId().equals(semester.getAcademicYear().getId())) {
                        continue;
                    }
                }

                Optional<ClassSubject> csOpt = classSubjectRepository.findBySchoolClassIdAndSubjectIdAndSemesterId(
                        ta.getSchoolClass().getId(), ta.getSubject().getId(), semester.getId());

                if (csOpt.isPresent() && csOpt.get().getPeriodsPerWeek() != null) {
                    totalAssignedPeriods += csOpt.get().getPeriodsPerWeek();
                } else {
                    log.warn("SCH-03: Giáo viên '{}' (ID: {}) có phân công '{}' cho lớp '{}' nhưng không có cấu hình ClassSubject trong học kỳ '{}' (ID: {}).",
                            teacher.getFullName(), teacher.getId(), ta.getSubjectName(),
                            ta.getSchoolClass().getName(), semester.getName(), semester.getId());
                }
            }
        }

        String academicYearName = semester.getAcademicYear() != null ? semester.getAcademicYear().getName() : "";
        Long academicYearId = semester.getAcademicYear() != null ? semester.getAcademicYear().getId() : null;
        PositionCode posCode = (teacher.getPosition() != null) ? teacher.getPosition().getCode() : null;

        // 1. Xác định Định mức tiết dạy chuẩn (Thông tư 05/2025/TT-BGDĐT: Giáo viên THCS = 19 tiết/tuần)
        int standardHours = DEFAULT_STANDARD_NORM_THCS;
        if (posCode == PositionCode.PRINCIPAL) {
            standardHours = 2;
        } else if (posCode == PositionCode.VICE_PRINCIPAL) {
            standardHours = 4;
        } else if (teacher.getPosition() != null) {
            List<TeachingNorm> norms = teachingNormRepository.findByAcademicYear(academicYearName);
            for (TeachingNorm norm : norms) {
                if (norm.getPosition() != null && norm.getPosition().getId().equals(teacher.getPosition().getId())) {
                    if (norm.getStandardHours() != null && norm.getStandardHours() > 0) {
                        standardHours = norm.getStandardHours();
                    }
                    break;
                }
            }
        }

        // 2. Tính giảm trừ kiêm nhiệm theo Mục 3.2 & 4.2 (Tối đa 2 nhiệm vụ có mức giảm cao nhất: LARGE(F2:H2, 1) + LARGE(F2:H2, 2))
        List<Double> reductions = new ArrayList<>();
        List<String> reductionLabels = new ArrayList<>();

        // Kiêm nhiệm 1: Giáo viên chủ nhiệm lớp (-4 tiết/tuần)
        if (academicYearId != null) {
            List<SchoolClass> homeroomClasses = schoolClassRepository.findByAcademicYearId(academicYearId).stream()
                    .filter(c -> c.getStatus() == ClassStatus.ACTIVE
                            && c.getHomeroomTeacher() != null
                            && c.getHomeroomTeacher().getId().equals(teacher.getId()))
                    .toList();
            if (!homeroomClasses.isEmpty()) {
                reductions.add(4.0);
                reductionLabels.add("GVCN lớp " + homeroomClasses.get(0).getName() + " (-4 tiết)");
            }
        }

        // Kiêm nhiệm 2: Tổ trưởng chuyên môn / Trưởng bộ môn (-3 tiết/tuần)
        boolean isHod = (posCode == PositionCode.HEAD_OF_DEPARTMENT)
                || (teacher.getDepartment() != null
                    && teacher.getDepartment().getManager() != null
                    && teacher.getDepartment().getManager().getId().equals(teacher.getId()));
        if (isHod) {
            reductions.add(3.0);
            reductionLabels.add("Tổ trưởng CM (-3 tiết)");
        }

        // Chỉ cộng tối đa 2 mức giảm trừ cao nhất
        reductions.sort(Comparator.reverseOrder());
        double totalReduction = reductions.stream().limit(2).mapToDouble(Double::doubleValue).sum();
        double effectiveNormHours = Math.max(2.0, standardHours - totalReduction);

        double diff = totalAssignedPeriods - effectiveNormHours;
        double yearlyDiff35Weeks = diff * WEEKS_PER_ACADEMIC_YEAR;

        String status;
        if (diff > 0.0) {
            status = "OVERLOAD";
        } else if (diff < 0.0) {
            status = "UNDERLOAD";
        } else {
            status = "BALANCED";
        }

        // Công thức kiểm soát trần thanh toán thừa giờ Mục 4.2: =IF(L2 > 200, "Vượt trần 200 tiết", IF(L2 < 0, "Thiếu định mức", "Hợp lệ"))
        String complianceNote;
        if (diff > MAX_WEEKLY_OVERTIME_50_PCT) {
            complianceNote = "Vượt giới hạn 50% ĐM (> 9.5 tiết/tuần)";
        } else if (yearlyDiff35Weeks > MAX_PAID_OVERTIME_PERIODS_PER_YEAR) {
            complianceNote = "Vượt trần 200 tiết/năm (TT 21/2025)";
        } else if (yearlyDiff35Weeks < 0) {
            complianceNote = "Thiếu định mức (" + (int) Math.abs(diff) + " tiết/tuần)";
        } else {
            complianceNote = "Hợp lệ (Trong trần 200 tiết/năm)";
        }

        return TeacherWorkloadResponse.builder()
                .teacherId(teacher.getId())
                .teacherName(teacher.getFullName())
                .positionName(teacher.getPosition() != null ? teacher.getPosition().getName() : null)
                .departmentName(teacher.getDepartment() != null ? teacher.getDepartment().getName() : null)
                .academicYear(academicYearName)
                .semesterId(semester.getId())
                .semesterName(semester.getName())
                .totalAssignedPeriods(totalAssignedPeriods)
                .standardHours(standardHours)
                .reductionHours(totalReduction)
                .reductionDetails(reductionLabels.isEmpty() ? "Không kiêm nhiệm" : String.join(", ", reductionLabels))
                .effectiveNormHours(effectiveNormHours)
                .difference(diff)
                .yearlyDifference35Weeks(yearlyDiff35Weeks)
                .workloadStatus(status)
                .complianceNote(complianceNote)
                .build();
    }
}
