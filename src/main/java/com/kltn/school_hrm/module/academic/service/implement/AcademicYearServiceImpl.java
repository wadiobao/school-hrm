package com.kltn.school_hrm.module.academic.service.implement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.academic.dto.request.AcademicYearRequest;
import com.kltn.school_hrm.module.academic.dto.response.AcademicYearResponse;
import com.kltn.school_hrm.module.academic.entity.AcademicYear;
import com.kltn.school_hrm.module.academic.repository.AcademicYearRepository;
import com.kltn.school_hrm.module.academic.service.AcademicYearService;
import com.kltn.school_hrm.shared.enums.Enums.AcademicStatus;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;
import com.kltn.school_hrm.shared.exception.custom.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Service xử lý logic nghiệp vụ cho năm học.
 * Đảm bảo các quy tắc ngày tháng (BR-01) và tính duy nhất của tên năm học.
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AcademicYearServiceImpl implements AcademicYearService {

    private final AcademicYearRepository academicYearRepository;

    @Override
    public AcademicYearResponse create(AcademicYearRequest request) {
        validateDateRange(request.getStartDate(), request.getEndDate());

        if (academicYearRepository.existsByName(request.getName())) {
            throw new BusinessException("Tên năm học đã tồn tại: " + request.getName());
        }

        AcademicYear entity = AcademicYear.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus())
                .build();

        return mapToResponse(academicYearRepository.save(entity));
    }

    @Override
    public AcademicYearResponse update(Long id, AcademicYearRequest request) {
        AcademicYear entity = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy năm học với id: " + id));

        validateDateRange(request.getStartDate(), request.getEndDate());

        if (academicYearRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new BusinessException("Tên năm học đã tồn tại: " + request.getName());
        }

        entity.setName(request.getName());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setStatus(request.getStatus());

        return mapToResponse(academicYearRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicYearResponse getById(Long id) {
        return academicYearRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy năm học với id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicYearResponse getActiveYear() {
        return academicYearRepository.findFirstByStatus(AcademicStatus.ACTIVE)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy năm học đang hoạt động"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcademicYearResponse> getAll() {
        return academicYearRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (!academicYearRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy năm học với id: " + id);
        }
        academicYearRepository.deleteById(id);
    }

    private void validateDateRange(java.time.LocalDate start, java.time.LocalDate end) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new BusinessException("Ngày kết thúc năm học phải sau ngày bắt đầu");
        }
    }

    private AcademicYearResponse mapToResponse(AcademicYear entity) {
        return AcademicYearResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

