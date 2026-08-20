package com.kltn.school_hrm.service;

import java.util.List;

import com.kltn.school_hrm.dto.request.TeachingAssignmentRequest;
import com.kltn.school_hrm.dto.response.TeachingAssignmentResponse;
import com.kltn.school_hrm.enums.Enums.Curriculum;

public interface TeachingAssignmentService {
    TeachingAssignmentResponse create(TeachingAssignmentRequest request);
    TeachingAssignmentResponse update(Long id, TeachingAssignmentRequest request);
    TeachingAssignmentResponse getById(Long id);
    List<TeachingAssignmentResponse> getAll();
    List<TeachingAssignmentResponse> getByTeacherId(Long teacherId);
    List<TeachingAssignmentResponse> getByTeacherIdAndCurriculum(Long teacherId, Curriculum curriculum);
    void delete(Long id);
}
