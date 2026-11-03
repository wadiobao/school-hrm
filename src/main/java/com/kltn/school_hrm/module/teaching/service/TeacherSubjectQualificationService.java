package com.kltn.school_hrm.module.teaching.service;

import java.util.List;

import com.kltn.school_hrm.module.teaching.dto.request.TeacherSubjectQualificationRequest;
import com.kltn.school_hrm.module.teaching.dto.response.TeacherSubjectQualificationResponse;

public interface TeacherSubjectQualificationService {
    TeacherSubjectQualificationResponse create(TeacherSubjectQualificationRequest request);
    TeacherSubjectQualificationResponse update(Long id, TeacherSubjectQualificationRequest request);
    TeacherSubjectQualificationResponse getById(Long id);
    List<TeacherSubjectQualificationResponse> getByTeacherId(Long teacherId);
    List<TeacherSubjectQualificationResponse> getBySubjectId(Long subjectId);
    void delete(Long id);
}

