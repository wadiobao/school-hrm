package com.kltn.school_hrm.module.academic.service.implement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.module.academic.dto.request.ClassroomRequest;
import com.kltn.school_hrm.module.academic.dto.response.ClassroomResponse;
import com.kltn.school_hrm.module.academic.entity.Classroom;
import com.kltn.school_hrm.module.academic.repository.ClassroomRepository;
import com.kltn.school_hrm.module.academic.service.ClassroomService;
import com.kltn.school_hrm.shared.enums.Enums.ClassroomStatus;
import com.kltn.school_hrm.shared.enums.Enums.ClassroomType;
import com.kltn.school_hrm.shared.exception.custom.BusinessException;
import com.kltn.school_hrm.shared.exception.custom.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Service quản lý danh mục phòng học, cơ sở vật chất.
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepository;

    @Override
    public ClassroomResponse create(ClassroomRequest request) {
        if (classroomRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Mã phòng học đã tồn tại: " + request.getCode());
        }

        Classroom entity = Classroom.builder()
                .code(request.getCode())
                .name(request.getName())
                .capacity(request.getCapacity())
                .roomType(request.getRoomType())
                .status(request.getStatus())
                .build();

        return mapToResponse(classroomRepository.save(entity));
    }

    @Override
    public ClassroomResponse update(Long id, ClassroomRequest request) {
        Classroom entity = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng học với id: " + id));

        if (classroomRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new BusinessException("Mã phòng học đã tồn tại: " + request.getCode());
        }

        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setCapacity(request.getCapacity());
        entity.setRoomType(request.getRoomType());
        entity.setStatus(request.getStatus());

        return mapToResponse(classroomRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ClassroomResponse getById(Long id) {
        return classroomRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng học với id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public ClassroomResponse getByCode(String code) {
        return classroomRepository.findByCode(code)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng học với mã: " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassroomResponse> getAll() {
        return classroomRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassroomResponse> getByStatus(ClassroomStatus status) {
        return classroomRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassroomResponse> getByRoomType(ClassroomType roomType) {
        return classroomRepository.findByRoomType(roomType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (!classroomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy phòng học với id: " + id);
        }
        classroomRepository.deleteById(id);
    }

    private ClassroomResponse mapToResponse(Classroom entity) {
        return ClassroomResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .capacity(entity.getCapacity())
                .roomType(entity.getRoomType())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

