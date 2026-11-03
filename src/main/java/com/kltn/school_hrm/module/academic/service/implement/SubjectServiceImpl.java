package com.kltn.school_hrm.module.academic.service.implement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.academic.dto.request.SubjectRequest;
import com.kltn.school_hrm.module.academic.dto.response.SubjectResponse;
import com.kltn.school_hrm.module.academic.entity.Subject;
import com.kltn.school_hrm.module.academic.repository.SubjectRepository;
import com.kltn.school_hrm.module.academic.service.SubjectService;
import com.kltn.school_hrm.shared.enums.Enums.SubjectStatus;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;
import com.kltn.school_hrm.shared.exception.custom.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Service quản lý danh mục môn học.
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;

    @Override
    public SubjectResponse create(SubjectRequest request) {
        if (subjectRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Mã môn học đã tồn tại: " + request.getCode());
        }

        Subject entity = Subject.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus())
                .build();

        return mapToResponse(subjectRepository.save(entity));
    }

    @Override
    public SubjectResponse update(Long id, SubjectRequest request) {
        Subject entity = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với id: " + id));

        if (subjectRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new BusinessException("Mã môn học đã tồn tại: " + request.getCode());
        }

        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setStatus(request.getStatus());

        return mapToResponse(subjectRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getById(Long id) {
        return subjectRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getByCode(String code) {
        return subjectRepository.findByCode(code)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với mã: " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getAll() {
        return subjectRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getByStatus(SubjectStatus status) {
        return subjectRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy môn học với id: " + id);
        }
        subjectRepository.deleteById(id);
    }

    private SubjectResponse mapToResponse(Subject entity) {
        return SubjectResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

