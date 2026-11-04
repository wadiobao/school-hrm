package com.kltn.school_hrm.module.scheduling.service;

import java.util.List;

import com.kltn.school_hrm.module.scheduling.dto.request.TimetableEntryRequest;
import com.kltn.school_hrm.module.scheduling.dto.response.TimetableEntryResponse;

public interface TimetableEntryService {
    TimetableEntryResponse create(TimetableEntryRequest request);
    TimetableEntryResponse update(Long id, TimetableEntryRequest request);
    TimetableEntryResponse getById(Long id);
    List<TimetableEntryResponse> getByTimetableId(Long timetableId);
    void delete(Long id);
}

