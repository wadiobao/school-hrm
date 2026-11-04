package com.kltn.school_hrm.module.scheduling.service;

import java.util.List;

import com.kltn.school_hrm.module.scheduling.dto.request.TimeSlotRequest;
import com.kltn.school_hrm.module.scheduling.dto.response.TimeSlotResponse;
import com.kltn.school_hrm.shared.enums.Enums.TimeSlotSession;

public interface TimeSlotService {
    TimeSlotResponse create(TimeSlotRequest request);
    TimeSlotResponse update(Long id, TimeSlotRequest request);
    TimeSlotResponse getById(Long id);
    List<TimeSlotResponse> getAll();
    List<TimeSlotResponse> getBySession(TimeSlotSession session);
    void delete(Long id);
    void initDefaultSlotsIfEmpty();
}

