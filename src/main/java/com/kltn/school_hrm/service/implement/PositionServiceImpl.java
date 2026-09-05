package com.kltn.school_hrm.service.implement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kltn.school_hrm.dto.request.PositionCreateRequest;
import com.kltn.school_hrm.dto.response.PositionResponse;
import com.kltn.school_hrm.entity.core.Position;
import com.kltn.school_hrm.repository.PositionRepository;
import com.kltn.school_hrm.service.PositionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;

    @Override
    @Transactional
    public PositionResponse createPosition(PositionCreateRequest request) {
        if (positionRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Position code already exists: " + request.getCode());
        }

        Position position = Position.builder()
                .code(request.getCode())
                .name(request.getName())
                .positionAllowanceRate(request.getPositionAllowanceRate())
                .build();

        position = positionRepository.save(position);
        return mapToResponse(position);
    }

    @Override
    @Transactional
    public PositionResponse updatePosition(Long id, PositionCreateRequest request) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Position not found"));

        if (!position.getCode().equals(request.getCode()) && positionRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Position code already exists: " + request.getCode());
        }

        position.setCode(request.getCode());
        position.setName(request.getName());
        position.setPositionAllowanceRate(request.getPositionAllowanceRate());

        position = positionRepository.save(position);
        return mapToResponse(position);
    }

    @Override
    public PositionResponse getPositionById(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Position not found"));
        return mapToResponse(position);
    }

    @Override
    public List<PositionResponse> getAllPositions() {
        return positionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePosition(Long id) {
        if (!positionRepository.existsById(id)) {
            throw new RuntimeException("Position not found");
        }
        positionRepository.deleteById(id);
    }

    private PositionResponse mapToResponse(Position position) {
        return PositionResponse.builder()
                .id(position.getId())
                .code(position.getCode())
                .name(position.getName())
                .positionAllowanceRate(position.getPositionAllowanceRate())
                .build();
    }
}
