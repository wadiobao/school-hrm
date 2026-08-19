package com.kltn.school_hrm.service;

import java.util.List;

import com.kltn.school_hrm.dto.request.PositionCreateRequest;
import com.kltn.school_hrm.dto.response.PositionResponse;

public interface PositionService {
    PositionResponse createPosition(PositionCreateRequest request);
    PositionResponse updatePosition(Long id, PositionCreateRequest request);
    PositionResponse getPositionById(Long id);
    List<PositionResponse> getAllPositions();
    void deletePosition(Long id);
}
