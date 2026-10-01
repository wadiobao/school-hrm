package com.kltn.school_hrm.module.core.service;

import java.util.List;

import com.kltn.school_hrm.module.core.dto.request.PositionCreateRequest;
import com.kltn.school_hrm.module.core.dto.response.PositionResponse;

public interface PositionService {
    PositionResponse createPosition(PositionCreateRequest request);
    PositionResponse updatePosition(Long id, PositionCreateRequest request);
    PositionResponse getPositionById(Long id);
    List<PositionResponse> getAllPositions();
    void deletePosition(Long id);
}
