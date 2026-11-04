package com.kltn.school_hrm.module.scheduling.service;

import java.util.List;

import com.kltn.school_hrm.module.scheduling.constraint.ValidationResult;
import com.kltn.school_hrm.module.scheduling.dto.request.TimetableRequest;
import com.kltn.school_hrm.module.scheduling.dto.response.TimetableMatrixResponse;
import com.kltn.school_hrm.module.scheduling.dto.response.TimetableResponse;

public interface TimetableService {
    TimetableResponse create(TimetableRequest request);
    TimetableResponse getById(Long id);
    List<TimetableResponse> getByClassAndSemester(Long schoolClassId, Long semesterId);
    TimetableResponse getPublishedTimetable(Long schoolClassId, Long semesterId);
    TimetableMatrixResponse getMatrix(Long timetableId);
    ValidationResult validateTimetable(Long id);
    TimetableResponse publish(Long id);
    TimetableResponse createNewVersionFromExisting(Long timetableId);
    void delete(Long id);
}

