package com.kltn.school_hrm.module.academic.service;

import java.util.List;

import com.kltn.school_hrm.module.academic.dto.request.ClassSubjectRequest;
import com.kltn.school_hrm.module.academic.dto.response.ClassSubjectResponse;

public interface ClassSubjectService {
    ClassSubjectResponse create(ClassSubjectRequest request);
    ClassSubjectResponse update(Long id, ClassSubjectRequest request);
    ClassSubjectResponse getById(Long id);
    List<ClassSubjectResponse> getByClassId(Long schoolClassId);
    List<ClassSubjectResponse> getBySemesterId(Long semesterId);
    List<ClassSubjectResponse> getByClassIdAndSemesterId(Long schoolClassId, Long semesterId);
    void delete(Long id);
}

