package com.kltn.school_hrm.module.academic.service;

import java.util.List;

import com.kltn.school_hrm.module.academic.dto.request.SchoolClassRequest;
import com.kltn.school_hrm.module.academic.dto.response.SchoolClassResponse;

public interface SchoolClassService {
    SchoolClassResponse create(SchoolClassRequest request);
    SchoolClassResponse update(Long id, SchoolClassRequest request);
    SchoolClassResponse getById(Long id);
    List<SchoolClassResponse> getByAcademicYearId(Long academicYearId);
    List<SchoolClassResponse> getByAcademicYearIdAndGradeLevel(Long academicYearId, Integer gradeLevel);
    void delete(Long id);
}

