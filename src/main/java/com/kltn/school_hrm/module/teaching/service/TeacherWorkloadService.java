package com.kltn.school_hrm.module.teaching.service;

import java.util.List;

import com.kltn.school_hrm.module.teaching.dto.response.TeacherWorkloadResponse;

public interface TeacherWorkloadService {
    TeacherWorkloadResponse getTeacherWorkload(Long teacherId, Long semesterId);
    List<TeacherWorkloadResponse> getWorkloadsBySemester(Long semesterId);
}

