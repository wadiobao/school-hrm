package com.kltn.school_hrm.module.academic.service;

import java.util.List;

import com.kltn.school_hrm.module.academic.dto.request.SemesterRequest;
import com.kltn.school_hrm.module.academic.dto.response.SemesterResponse;

public interface SemesterService {
    SemesterResponse create(SemesterRequest request);
    SemesterResponse update(Long id, SemesterRequest request);
    SemesterResponse getById(Long id);
    List<SemesterResponse> getByAcademicYearId(Long academicYearId);
    void delete(Long id);
}

