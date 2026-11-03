package com.kltn.school_hrm.module.academic.service.implement;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.academic.dto.request.SemesterRequest;
import com.kltn.school_hrm.module.academic.dto.response.SemesterResponse;
import com.kltn.school_hrm.module.academic.entity.AcademicYear;
import com.kltn.school_hrm.module.academic.entity.Semester;
import com.kltn.school_hrm.module.academic.repository.AcademicYearRepository;
import com.kltn.school_hrm.module.academic.repository.SemesterRepository;
import com.kltn.school_hrm.module.academic.service.SemesterService;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;
import com.kltn.school_hrm.shared.exception.custom.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Service xử lý logic nghiệp vụ cho học kỳ.
 * Đảm bảo các quy tắc:
 * - BR-01: Ngày kết thúc lớn hơn ngày bắt đầu.
 * - BR-02: Thời gian học kỳ phải nằm trọn vẹn trong năm học.
 * - BR-03: Các học kỳ trong cùng năm học không được chồng lấn thời gian.
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class SemesterServiceImpl implements SemesterService {

    private final SemesterRepository semesterRepository;
    private final AcademicYearRepository academicYearRepository;

    @Override
    public SemesterResponse create(SemesterRequest request) {
        AcademicYear year = academicYearRepository.findById(request.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy năm học với id: " + request.getAcademicYearId()));

        validateSemesterDates(request.getStartDate(), request.getEndDate(), year, null);

        Semester entity = Semester.builder()
                .academicYear(year)
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus())
                .build();

        return mapToResponse(semesterRepository.save(entity));
    }

    @Override
    public SemesterResponse update(Long id, SemesterRequest request) {
        Semester entity = semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học kỳ với id: " + id));

        AcademicYear year = academicYearRepository.findById(request.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy năm học với id: " + request.getAcademicYearId()));

        validateSemesterDates(request.getStartDate(), request.getEndDate(), year, id);

        entity.setAcademicYear(year);
        entity.setName(request.getName());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setStatus(request.getStatus());

        return mapToResponse(semesterRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public SemesterResponse getById(Long id) {
        return semesterRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học kỳ với id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SemesterResponse> getByAcademicYearId(Long academicYearId) {
        return semesterRepository.findByAcademicYearIdOrderByStartDateAsc(academicYearId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (!semesterRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy học kỳ với id: " + id);
        }
        semesterRepository.deleteById(id);
    }

    private void validateSemesterDates(LocalDate start, LocalDate end, AcademicYear year, Long currentSemesterId) {
        if (start == null || end == null) {
            throw new BusinessException("Ngày bắt đầu và kết thúc học kỳ không được để trống");
        }
        if (!end.isAfter(start)) {
            throw new BusinessException("Ngày kết thúc học kỳ phải sau ngày bắt đầu");
        }

        // BR-02: Thời gian học kỳ phải nằm trong năm học
        if (start.isBefore(year.getStartDate()) || end.isAfter(year.getEndDate())) {
            throw new BusinessException(String.format(
                    "Thời gian học kỳ (%s đến %s) phải nằm trong khoảng thời gian năm học (%s đến %s)",
                    start, end, year.getStartDate(), year.getEndDate()));
        }

        // BR-03: Không chồng lấn với học kỳ khác trong cùng năm học
        List<Semester> existingSemesters = semesterRepository.findByAcademicYearId(year.getId());
        for (Semester s : existingSemesters) {
            if (currentSemesterId != null && s.getId().equals(currentSemesterId)) {
                continue;
            }
            // Overlap check: !(end < s.start || start > s.end)
            boolean overlaps = !(end.isBefore(s.getStartDate()) || start.isAfter(s.getEndDate()));
            if (overlaps) {
                throw new BusinessException(String.format(
                        "Thời gian học kỳ bị chồng lấn với học kỳ '%s' (%s đến %s)",
                        s.getName(), s.getStartDate(), s.getEndDate()));
            }
        }
    }

    private SemesterResponse mapToResponse(Semester entity) {
        return SemesterResponse.builder()
                .id(entity.getId())
                .academicYearId(entity.getAcademicYear() != null ? entity.getAcademicYear().getId() : null)
                .academicYearName(entity.getAcademicYear() != null ? entity.getAcademicYear().getName() : null)
                .name(entity.getName())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

