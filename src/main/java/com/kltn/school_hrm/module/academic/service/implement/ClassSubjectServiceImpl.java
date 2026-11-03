package com.kltn.school_hrm.module.academic.service.implement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.academic.dto.request.ClassSubjectRequest;
import com.kltn.school_hrm.module.academic.dto.response.ClassSubjectResponse;
import com.kltn.school_hrm.module.academic.entity.ClassSubject;
import com.kltn.school_hrm.module.academic.entity.SchoolClass;
import com.kltn.school_hrm.module.academic.entity.Semester;
import com.kltn.school_hrm.module.academic.entity.Subject;
import com.kltn.school_hrm.module.academic.repository.ClassSubjectRepository;
import com.kltn.school_hrm.module.academic.repository.SchoolClassRepository;
import com.kltn.school_hrm.module.academic.repository.SemesterRepository;
import com.kltn.school_hrm.module.academic.repository.SubjectRepository;
import com.kltn.school_hrm.module.academic.service.ClassSubjectService;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;
import com.kltn.school_hrm.shared.exception.custom.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Service quản lý phân phối chương trình học theo lớp và học kỳ.
 * Đảm bảo:
 * - BR-05: Số tiết mỗi môn trong lớp phải lớn hơn 0.
 * - BR-06: Một lớp không được khai báo trùng môn trong cùng một học kỳ.
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ClassSubjectServiceImpl implements ClassSubjectService {

    private final ClassSubjectRepository classSubjectRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;

    @Override
    public ClassSubjectResponse create(ClassSubjectRequest request) {
        if (request.getPeriodsPerWeek() == null || request.getPeriodsPerWeek() <= 0) {
            throw new BusinessException("Số tiết mỗi tuần phải lớn hơn 0 (BR-05)");
        }

        SchoolClass schoolClass = schoolClassRepository.findById(request.getSchoolClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với id: " + request.getSchoolClassId()));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với id: " + request.getSubjectId()));

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học kỳ với id: " + request.getSemesterId()));

        // BR-06: Một lớp không được khai báo trùng môn trong cùng học kỳ
        if (classSubjectRepository.existsBySchoolClassIdAndSubjectIdAndSemesterId(
                request.getSchoolClassId(), request.getSubjectId(), request.getSemesterId())) {
            throw new BusinessException(String.format(
                    "Lớp '%s' đã có môn '%s' trong học kỳ '%s' (BR-06)",
                    schoolClass.getName(), subject.getName(), semester.getName()));
        }

        ClassSubject entity = ClassSubject.builder()
                .schoolClass(schoolClass)
                .subject(subject)
                .semester(semester)
                .periodsPerWeek(request.getPeriodsPerWeek())
                .build();

        return mapToResponse(classSubjectRepository.save(entity));
    }

    @Override
    public ClassSubjectResponse update(Long id, ClassSubjectRequest request) {
        ClassSubject entity = classSubjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân phối môn học với id: " + id));

        if (request.getPeriodsPerWeek() == null || request.getPeriodsPerWeek() <= 0) {
            throw new BusinessException("Số tiết mỗi tuần phải lớn hơn 0 (BR-05)");
        }

        SchoolClass schoolClass = schoolClassRepository.findById(request.getSchoolClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với id: " + request.getSchoolClassId()));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với id: " + request.getSubjectId()));

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học kỳ với id: " + request.getSemesterId()));

        // BR-06 check
        if (classSubjectRepository.existsBySchoolClassIdAndSubjectIdAndSemesterIdAndIdNot(
                request.getSchoolClassId(), request.getSubjectId(), request.getSemesterId(), id)) {
            throw new BusinessException(String.format(
                    "Lớp '%s' đã có môn '%s' trong học kỳ '%s' (BR-06)",
                    schoolClass.getName(), subject.getName(), semester.getName()));
        }

        entity.setSchoolClass(schoolClass);
        entity.setSubject(subject);
        entity.setSemester(semester);
        entity.setPeriodsPerWeek(request.getPeriodsPerWeek());

        return mapToResponse(classSubjectRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ClassSubjectResponse getById(Long id) {
        return classSubjectRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân phối môn học với id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassSubjectResponse> getByClassId(Long schoolClassId) {
        return classSubjectRepository.findBySchoolClassId(schoolClassId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassSubjectResponse> getBySemesterId(Long semesterId) {
        return classSubjectRepository.findBySemesterId(semesterId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassSubjectResponse> getByClassIdAndSemesterId(Long schoolClassId, Long semesterId) {
        return classSubjectRepository.findBySchoolClassIdAndSemesterId(schoolClassId, semesterId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (!classSubjectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy phân phối môn học với id: " + id);
        }
        classSubjectRepository.deleteById(id);
    }

    private ClassSubjectResponse mapToResponse(ClassSubject entity) {
        return ClassSubjectResponse.builder()
                .id(entity.getId())
                .schoolClassId(entity.getSchoolClass() != null ? entity.getSchoolClass().getId() : null)
                .schoolClassName(entity.getSchoolClass() != null ? entity.getSchoolClass().getName() : null)
                .subjectId(entity.getSubject() != null ? entity.getSubject().getId() : null)
                .subjectCode(entity.getSubject() != null ? entity.getSubject().getCode() : null)
                .subjectName(entity.getSubject() != null ? entity.getSubject().getName() : null)
                .semesterId(entity.getSemester() != null ? entity.getSemester().getId() : null)
                .semesterName(entity.getSemester() != null ? entity.getSemester().getName() : null)
                .periodsPerWeek(entity.getPeriodsPerWeek())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

