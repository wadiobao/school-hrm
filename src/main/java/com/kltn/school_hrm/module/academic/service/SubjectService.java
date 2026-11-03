package com.kltn.school_hrm.module.academic.service;

import java.util.List;

import com.kltn.school_hrm.module.academic.dto.request.SubjectRequest;
import com.kltn.school_hrm.module.academic.dto.response.SubjectResponse;
import com.kltn.school_hrm.shared.enums.Enums.SubjectStatus;

public interface SubjectService {
    SubjectResponse create(SubjectRequest request);
    SubjectResponse update(Long id, SubjectRequest request);
    SubjectResponse getById(Long id);
    SubjectResponse getByCode(String code);
    List<SubjectResponse> getAll();
    List<SubjectResponse> getByStatus(SubjectStatus status);
    void delete(Long id);
}

