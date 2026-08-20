package com.kltn.school_hrm.service;

import java.util.List;

import com.kltn.school_hrm.dto.request.TeachingLogRequest;
import com.kltn.school_hrm.dto.request.TeachingNormRequest;
import com.kltn.school_hrm.dto.response.TeachingLogResponse;
import com.kltn.school_hrm.dto.response.TeachingNormResponse;
import com.kltn.school_hrm.enums.Enums.RequestStatus;

public interface TeachingService {

    // --- TeachingLog ---
    TeachingLogResponse createLog(TeachingLogRequest request);
    TeachingLogResponse updateLog(Long id, TeachingLogRequest request);
    TeachingLogResponse getLogById(Long id);
    List<TeachingLogResponse> getAllLogs();
    List<TeachingLogResponse> getLogsByAssignmentId(Long assignmentId);
    List<TeachingLogResponse> getLogsByTeacherId(Long teacherId);
    List<TeachingLogResponse> getLogsByStatus(RequestStatus status);
    void deleteLog(Long id);

    // --- TeachingNorm ---
    TeachingNormResponse createNorm(TeachingNormRequest request);
    TeachingNormResponse updateNorm(Long id, TeachingNormRequest request);
    TeachingNormResponse getNormById(Long id);
    List<TeachingNormResponse> getAllNorms();
    List<TeachingNormResponse> getNormsByAcademicYear(String academicYear);
    List<TeachingNormResponse> getNormsByPositionId(Long positionId);
    void deleteNorm(Long id);
}
