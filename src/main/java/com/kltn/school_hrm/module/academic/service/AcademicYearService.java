package com.kltn.school_hrm.module.academic.service;

import java.util.List;

import com.kltn.school_hrm.module.academic.dto.request.AcademicYearRequest;
import com.kltn.school_hrm.module.academic.dto.response.AcademicYearResponse;

public interface AcademicYearService {
    AcademicYearResponse create(AcademicYearRequest request);
    AcademicYearResponse update(Long id, AcademicYearRequest request);
    AcademicYearResponse getById(Long id);
    AcademicYearResponse getActiveYear();
    List<AcademicYearResponse> getAll();
    void delete(Long id);
}

