package com.kltn.school_hrm.module.academic.service;

import java.util.List;

import com.kltn.school_hrm.module.academic.dto.request.ClassroomRequest;
import com.kltn.school_hrm.module.academic.dto.response.ClassroomResponse;
import com.kltn.school_hrm.shared.enums.Enums.ClassroomStatus;
import com.kltn.school_hrm.shared.enums.Enums.ClassroomType;

public interface ClassroomService {
    ClassroomResponse create(ClassroomRequest request);
    ClassroomResponse update(Long id, ClassroomRequest request);
    ClassroomResponse getById(Long id);
    ClassroomResponse getByCode(String code);
    List<ClassroomResponse> getAll();
    List<ClassroomResponse> getByStatus(ClassroomStatus status);
    List<ClassroomResponse> getByRoomType(ClassroomType roomType);
    void delete(Long id);
}

