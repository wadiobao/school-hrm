package com.kltn.school_hrm.module.academic.service.implement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.academic.dto.request.SchoolClassRequest;
import com.kltn.school_hrm.module.academic.dto.response.SchoolClassResponse;
import com.kltn.school_hrm.module.academic.entity.AcademicYear;
import com.kltn.school_hrm.module.academic.entity.SchoolClass;
import com.kltn.school_hrm.module.academic.repository.AcademicYearRepository;
import com.kltn.school_hrm.module.academic.repository.SchoolClassRepository;
import com.kltn.school_hrm.module.academic.service.SchoolClassService;
import com.kltn.school_hrm.module.employee.entity.Employee;
import com.kltn.school_hrm.module.employee.repository.EmployeeRepository;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;
import com.kltn.school_hrm.shared.exception.custom.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Service quản lý lớp học.
 * Đảm bảo:
 * - BR-04: Lớp học phải thuộc một năm học hợp lệ.
 * - Tên lớp trong cùng một năm học phải là duy nhất.
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class SchoolClassServiceImpl implements SchoolClassService {

    private final SchoolClassRepository schoolClassRepository;
    private final AcademicYearRepository academicYearRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public SchoolClassResponse create(SchoolClassRequest request) {
        AcademicYear year = academicYearRepository.findById(request.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy năm học với id: " + request.getAcademicYearId()));

        if (schoolClassRepository.existsByNameAndAcademicYearId(request.getName(), request.getAcademicYearId())) {
            throw new BusinessException(String.format("Lớp '%s' đã tồn tại trong năm học này", request.getName()));
        }

        Employee teacher = null;
        if (request.getHomeroomTeacherId() != null) {
            teacher = employeeRepository.findById(request.getHomeroomTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giáo viên với id: " + request.getHomeroomTeacherId()));
        }

        SchoolClass entity = SchoolClass.builder()
                .name(request.getName())
                .gradeLevel(request.getGradeLevel())
                .academicYear(year)
                .homeroomTeacher(teacher)
                .capacity(request.getCapacity())
                .status(request.getStatus())
                .build();

        return mapToResponse(schoolClassRepository.save(entity));
    }

    @Override
    public SchoolClassResponse update(Long id, SchoolClassRequest request) {
        SchoolClass entity = schoolClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với id: " + id));

        AcademicYear year = academicYearRepository.findById(request.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy năm học với id: " + request.getAcademicYearId()));

        if (schoolClassRepository.existsByNameAndAcademicYearIdAndIdNot(request.getName(), request.getAcademicYearId(), id)) {
            throw new BusinessException(String.format("Lớp '%s' đã tồn tại trong năm học này", request.getName()));
        }

        Employee teacher = null;
        if (request.getHomeroomTeacherId() != null) {
            teacher = employeeRepository.findById(request.getHomeroomTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giáo viên với id: " + request.getHomeroomTeacherId()));
        }

        entity.setName(request.getName());
        entity.setGradeLevel(request.getGradeLevel());
        entity.setAcademicYear(year);
        entity.setHomeroomTeacher(teacher);
        entity.setCapacity(request.getCapacity());
        entity.setStatus(request.getStatus());

        return mapToResponse(schoolClassRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolClassResponse getById(Long id) {
        return schoolClassRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolClassResponse> getByAcademicYearId(Long academicYearId) {
        return schoolClassRepository.findByAcademicYearId(academicYearId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolClassResponse> getByAcademicYearIdAndGradeLevel(Long academicYearId, Integer gradeLevel) {
        return schoolClassRepository.findByAcademicYearIdAndGradeLevel(academicYearId, gradeLevel).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (!schoolClassRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy lớp học với id: " + id);
        }
        schoolClassRepository.deleteById(id);
    }

    private SchoolClassResponse mapToResponse(SchoolClass entity) {
        return SchoolClassResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .gradeLevel(entity.getGradeLevel())
                .academicYearId(entity.getAcademicYear() != null ? entity.getAcademicYear().getId() : null)
                .academicYearName(entity.getAcademicYear() != null ? entity.getAcademicYear().getName() : null)
                .homeroomTeacherId(entity.getHomeroomTeacher() != null ? entity.getHomeroomTeacher().getId() : null)
                .homeroomTeacherName(entity.getHomeroomTeacher() != null ? entity.getHomeroomTeacher().getFullName() : null)
                .capacity(entity.getCapacity())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

